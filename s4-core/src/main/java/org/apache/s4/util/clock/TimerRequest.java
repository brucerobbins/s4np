/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.s4.util.clock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TimerRequest {
    private long targetTime;
    private BlockingQueue<Long> blockingQueue = new LinkedBlockingQueue<Long>();

    public TimerRequest(long targetTime) {
        this.targetTime = targetTime;
    }

    public long getTargetTime() {
        return targetTime;
    }

    public void wakeUp(long currentTime) {
        blockingQueue.add(currentTime);
    }

    public long waitForTargetTime() {
        try {
            return blockingQueue.take();
        } catch (InterruptedException ie) {
            return -1;
        }
    }
}

