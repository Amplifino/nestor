/*
 * Copyright (c) Amplifino (2015). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amplifino.nestor.logging.impl;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.osgi.service.log.LogService;

class LogHandler extends Handler {

	private final LogService logService;

    LogHandler(LogService logService) {
    	super();
		this.logService = logService;		
	}

	@Override
	public void publish(LogRecord logRecord) {
		if (!isLoggable(logRecord)) {
			return;
		}
		logService.log(mapLevel(logRecord.getLevel()), format(logRecord) , logRecord.getThrown());
	}

    private String format(LogRecord record) {
        return record.getMessage() + " (" + record.getLoggerName() + ")";    
    }

    private int mapLevel(Level level) {
        if (level.intValue() <= Level.FINE.intValue()) {
            return LogService.LOG_DEBUG;
        } else if (level.intValue() <= Level.INFO.intValue()) {
            return LogService.LOG_INFO;
        } else if (level.intValue() <= Level.WARNING.intValue()) {
            return LogService.LOG_WARNING;
        } else {
            return LogService.LOG_ERROR;
        }
    }

	@Override
	public void flush() {
	}

	@Override
	public void close()  {
	}
}
