/*
 * Copyright (c) Amplifino (2016). All Rights Reserved.
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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;
import org.osgi.service.metatype.annotations.Designate;

import com.amplifino.nestor.logging.LogBridge;

@Component(name="com.amplifino.nestor.logging" , immediate=true)
@Designate(ocd = BridgeConfiguration.class)
public class LogBridgeImpl implements LogBridge {
	
	private static final Logger LOGGER = Logger.getLogger("com.amplifino.osgi.logging");
    
    private volatile LogService logService;
    private volatile LogHandler handler;
	
	@Reference
	public void setLogService(LogService logService) {
        this.logService = logService;
	}
	
    @Activate
    public void activate(BridgeConfiguration configuration) {
        handler = new LogHandler(logService);
        handler.setLevel(Level.parse(configuration.level()));
        Logger.getLogger("").addHandler(handler);  
        LOGGER.info("Started forwarding from java.util.logging to log service ");
    }

    @Deactivate
    public void deactivate() {
    	LOGGER.info("Stopped forwarding from java.util.logging to log service ");
        Logger.getLogger("").removeHandler(handler);
    }

	@Override
	public void setLevel(Level level) {
		handler.setLevel(Objects.requireNonNull(level));		
	}

}
