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

package com.amplifino.nestor.rest.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import com.amplifino.nestor.rest.JerseyTracker;

@Component(name="com.amplifino.nestor.rest", service=WhiteboardConfigurationProvider.class)
@Designate(ocd = WhiteboardConfiguration.class)
public class WhiteboardConfigurationProvider {
	
	private WhiteboardConfiguration configuration;
	@Reference
	private JerseyTracker jerseyTracker;

	@Activate
	public void activate(WhiteboardConfiguration configuration) {
		this.configuration = configuration;
	}

	WhiteboardConfiguration configuration() {
		return configuration;
	}
}
