/*
 * Copyright (c) Amplifino (2015, 2016). All Rights Reserved.
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

package com.amplifino.nestor.jaxrs;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.hk2.osgiresourcelocator.ServiceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/*
 * This component waits for Jersey initialization.
 * 
 * 1) Wait for hk2 (Jersey's injector) to become initialized
 * to avoid starting applications while HK2 is still in the resolved state
 * 
 * 2) Wait for jersey bundles to start, so these bundles correctly detect they are running in an osgi environment.
 * 
 */
@Component
public class JerseyTrackerProvider {
	
	private static final int stateMask =
            Bundle.INSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING;
    
	private volatile BundleTracker<Bundle> tracker;
    private volatile BundleContext context;
	private volatile ServiceRegistration<JerseyTracker> registration;

	@Activate
	public void activate(BundleContext context) {		
		this.context = context;
		// we need to wait for jersey and hk2 bundles to be active (started)
		Set<Bundle> bundles = Stream.concat(jerseyBundles(), hk2Bundles())
			.filter(b -> b.getState() != Bundle.ACTIVE)
			.collect(Collectors.toSet());
		if (bundles.isEmpty()) {
			register();
		} else {
			this.tracker = new BundleTracker<>(context, stateMask, new BundleWaiter(bundles));
			tracker.open();
		}
	}
	
	Stream<Bundle> jerseyBundles() {
		return Arrays.stream(context.getBundles())
			.filter(b -> b.getSymbolicName().startsWith("org.glassfish.jersey"));
	}
	
	Stream<Bundle> hk2Bundles() {
		// find the hk2 osgi resource locator bundle, in order to wait for it to become active
    	// if hk2 is still in resolved state , we risk running HK2 initialization before activator has run
    	// but loading ServiceLoader looks safe (abstract class without static blocks).
		// as we have loaded a class from the HK2 bundle, this will cause the bundle, which has the lazy activation option,
		return Stream.of(FrameworkUtil.getBundle(ServiceLoader.class));
	}
	
	@Deactivate
	public synchronized void deactivate() {
		if (tracker != null) {
			tracker.close();
		}
		if (registration != null) {
			registration.unregister();
			registration = null;
		}		
	}

	private synchronized void start() {
		if (tracker != null) {
			tracker.close();
			register();
			tracker = null;
		}
	}
	
	private void register() {
		registration = context.registerService(JerseyTracker.class, new JerseyTrackerImpl(), null);
	}
	
    private class BundleWaiter implements BundleTrackerCustomizer<Bundle> {
    	private final Set<Bundle> targets;
    	private final Phaser phaser;
    	
    	private BundleWaiter(Set<Bundle> targets) {
    		this.targets = targets;
    		phaser = new Phaser(targets.size()) {
    			@Override
    			protected boolean onAdvance(int phase, int registeredParties) {    				
    				start();
    				return true;
    			}
    		};
    	}
    	
    	@Override
        public Bundle addingBundle(Bundle bundle, BundleEvent event) {    		
    		if (!targets.contains(bundle)) {
                return null;
    		} else if (bundle.getState() == Bundle.ACTIVE) {   
    			phaser.arrive();
    			return null;
    		}  else {             	
    			return bundle;
    		}
        }

	    @Override
        public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle ignore) {	    	
	    	if (event.getType() == BundleEvent.STARTED) {
	    		phaser.arrive();
	    	}
	    }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event, Bundle ignore) {
        }
        
    }
}
