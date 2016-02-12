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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(name = "Amplifino Logging Bridge")
@interface BridgeConfiguration {

	@AttributeDefinition(options={
		@Option(label="Severe",value="SEVERE"),
		@Option(label="Warning",value="WARNING"),
		@Option(label="Info",value="INFO"),
		@Option(label="Config",value="CONFIG"),
		@Option(label="Fine",value="FINE"),
		@Option(label="Finer",value="FINER"),
		@Option(label="Finest",value="FINEST")},
		description="Log level threshold for forwarding logRecords to log service")
	String level() default "INFO";
}
