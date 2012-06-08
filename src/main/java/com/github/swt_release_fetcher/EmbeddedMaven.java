/*
 * Copyright 2012 Uri Shaked
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.swt_release_fetcher;

import org.apache.maven.Maven;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.cli.PrintStreamLogger;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;

/**
 * This class holds a singleton of embedded maven configuration.
 * 
 * @author Uri Shaked
 */
public class EmbeddedMaven {
	private static final int LOG_LEVEL = Logger.LEVEL_DEBUG;

	private static Maven instance = null;

	private static Maven initMaven() {
		ContainerConfiguration cc = new DefaultContainerConfiguration().setName("maven");
		DefaultPlexusContainer container;
		try {
			container = new DefaultPlexusContainer(cc);
			PrintStreamLogger logger = new PrintStreamLogger(System.out);
			logger.setThreshold(LOG_LEVEL);
			container.setLoggerManager(new MavenLoggerManager(logger));
			return container.lookup(Maven.class);
		} catch (PlexusContainerException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (ComponentLookupException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static Maven get() {
		if (instance == null) {
			instance = initMaven();
		}
		
		return instance;
	}
}
