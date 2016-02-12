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

package com.amplifino.nestor.logging;

import java.util.logging.Level;

import org.osgi.annotation.versioning.ProviderType;

/**
 * 
 * Service to configure the Log Bride programmatically
 * (preferred way is using Configuration Admin)
 * Note that you can also reference this service from a DS component to
 * avoid race conditions at OSGI container startup in the unlikely
 * case that you do not want to start your component before log forwarding has started
 *  
 */

@ProviderType
public interface LogBridge {
	/**
	 * Change the bridge's handler  log level
	 */
	void setLevel(Level level);
}
