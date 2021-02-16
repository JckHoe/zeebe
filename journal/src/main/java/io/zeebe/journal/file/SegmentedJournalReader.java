/*
 * Copyright 2017-present Open Networking Foundation
 * Copyright © 2020 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.journal.file;

import io.zeebe.journal.JournalReader;
import io.zeebe.journal.JournalRecord;
import java.util.NoSuchElementException;

class SegmentedJournalReader implements JournalReader {

  private final SegmentedJournal journal;
  private JournalSegment currentSegment;
  private JournalRecord previousEntry;
  private MappedJournalSegmentReader currentReader;

  SegmentedJournalReader(final SegmentedJournal journal) {
    this.journal = journal;
    initialize();
  }

  /** Initializes the reader to the given index. */
  private void initialize() {
    currentSegment = journal.getFirstSegment();
    currentReader = currentSegment.createReader();
  }

  public long getCurrentIndex() {
    final long currentIndex = currentReader.getCurrentIndex();
    if (currentIndex != 0) {
      return currentIndex;
    }
    if (previousEntry != null) {
      return previousEntry.index();
    }
    return 0;
  }

  public JournalRecord getCurrentEntry() {
    final var currentEntry = currentReader.getCurrentEntry();
    if (currentEntry != null) {
      return currentEntry;
    }
    return previousEntry;
  }

  public long getNextIndex() {
    return currentReader.getNextIndex();
  }

  @Override
  public boolean hasNext() {
    return hasNextEntry();
  }

  @Override
  public JournalRecord next() {
    if (!currentReader.hasNext()) {
      final JournalSegment nextSegment = journal.getNextSegment(currentSegment.index());
      if (nextSegment != null && nextSegment.index() == getNextIndex()) {
        previousEntry = currentReader.getCurrentEntry();
        replaceCurrentSegment(nextSegment);
        return currentReader.next();
      } else {
        throw new NoSuchElementException();
      }
    } else {
      previousEntry = currentReader.getCurrentEntry();
      return currentReader.next();
    }
  }

  @Override
  public long seek(final long index) {
    // If the current segment is not open, it has been replaced. Reset the segments.
    if (!currentSegment.isOpen()) {
      seekToFirst();
    }

    if (index < currentReader.getNextIndex()) {
      rewind(index);
    } else if (index > currentReader.getNextIndex()) {
      forward(index);
    } else {
      currentReader.seek(index);
    }

    return getNextIndex();
  }

  @Override
  public long seekToFirst() {
    replaceCurrentSegment(journal.getFirstSegment());
    previousEntry = null;
    return journal.getFirstIndex();
  }

  @Override
  public long seekToLast() {
    replaceCurrentSegment(journal.getLastSegment());
    seek(journal.getLastIndex());

    return journal.getLastIndex();
  }

  @Override
  public long seekToAsqn(final long asqn) {
    final var journalIndex = journal.getJournalIndex();
    final var index = journalIndex.lookupAsqn(asqn);

    // depending on the type of index, it's possible there is no ASQN indexed, in which case start
    // from the beginning
    if (index == null) {
      seekToFirst();
    } else {
      seek(index);
    }

    // potential beneficiary of a peek() call, which would avoid the duplicate seek or
    // being at the second position if the first entry has a greater ASQN
    JournalRecord record = null;
    while (hasNext()) {
      final var currentRecord = next();
      if (currentRecord.asqn() <= asqn) {
        record = currentRecord;
      } else if (currentRecord.asqn() >= asqn) {
        break;
      }
    }

    // if the journal was empty, the reader will be at the beginning of the log
    // if the journal only contained entries with ASQN greater than the one requested, it will be at
    // the second entry (as it has read the first one)
    if (record == null) {
      return getNextIndex();
    }

    // This is needed so that the next() returns the correct record
    // TODO: Remove the duplicate seek. https://github.com/zeebe-io/zeebe/issues/6223
    return seek(record.index());
  }

  @Override
  public void close() {
    currentReader.close();
    journal.closeReader(this);
  }

  /** Rewinds the journal to the given index. */
  private void rewind(final long index) {
    if (currentSegment.index() >= index) {
      final long lookupIndex = index == Long.MIN_VALUE ? index : index - 1; // avoid underflow
      final JournalSegment segment = journal.getSegment(lookupIndex);
      if (segment != null) {
        replaceCurrentSegment(segment);
      }
    }

    currentReader.seek(index);
    previousEntry = currentReader.getCurrentEntry();
  }

  /** Fast forwards the journal to the given index. */
  private void forward(final long index) {
    // skip to the correct segment if there is one
    if (!currentSegment.equals(journal.getLastSegment())) {
      final JournalSegment segment = journal.getSegment(index);
      if (segment != null && !segment.equals(currentSegment)) {
        replaceCurrentSegment(segment);
      }
    }

    while (getNextIndex() < index && hasNext()) {
      next();
    }
  }

  private boolean hasNextEntry() {
    if (!currentReader.hasNext()) {
      final JournalSegment nextSegment = journal.getNextSegment(currentSegment.index());
      if (nextSegment != null && nextSegment.index() == getNextIndex()) {
        previousEntry = currentReader.getCurrentEntry();
        replaceCurrentSegment(nextSegment);
        return currentReader.hasNext();
      }
      return false;
    }
    return true;
  }

  private void replaceCurrentSegment(final JournalSegment nextSegment) {
    if (currentSegment.equals(nextSegment)) {
      currentReader.reset();
      return;
    }

    currentReader.close();
    currentSegment = nextSegment;
    currentReader = currentSegment.createReader();
  }
}
