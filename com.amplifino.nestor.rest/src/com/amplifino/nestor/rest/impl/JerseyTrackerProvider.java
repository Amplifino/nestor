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

package com.amplifino.nestor.rest.impl;

import org.glassfish.hk2.osgiresourcelocator.ServiceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import com.amplifino.nestor.rest.JerseyTracker;

/*
 * This component waits for Jersey initialization.
 * 
 * Wait for hk2 (Jersey's injector) to become initialized
 * to avoid starting applications while HK2 is still in the resolved state
 * 
 */
@Component
public class JerseyTrackerProvider {
	
	private static final int stateMask =
            Bundle.INSTALLED | Bundle.RESOLVED | Bundle.START_TRANSIENT | Bundle.STARTING | Bundle.ACTIVE |
            Bundle.STOP_TRANSIENT | Bundle.STOPPING;
    
	private volatile BundleTracker<Bundle> tracker;
    private volatile BundleContext context;
	private volatile ServiceRegistration<JerseyTracker> registration;

	public JerseyTrackerProvider() {
	}

	@Activate
	public void activate(BundleContext context) {		
		this.context = context;
		// find the hk2 osgi resource locator bundle, and wait for it to become active
    	// if hk2 is still in resolved state , we risk running HK2 initialization before activator has run
    	// but loading ServiceLoader looks safe (abstract class without static blocks).
		// as we have loaded a class from the HK2 bundle, this will cause the bundle, which has the lazy activation option,
		// to automatically go from the starting to the started state
		Bundle hk2Bundle = FrameworkUtil.getBundle(ServiceLoader.class);
		this.tracker = new BundleTracker<>(context, stateMask, new BundleWaiter(hk2Bundle));
		tracker.open();
	}
	
	@Deactivate
	public void deactivate() {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}		
	}

	private void start() {
		tracker.close();
		registration = context.registerService(JerseyTracker.class, new JerseyTrackerImpl(), null);
	}
	
    private class BundleWaiter implements BundleTrackerCustomizer<Bundle> {
    	private final Bundle target;
    	
    	private BundleWaiter(Bundle target) {
    		this.target = target;
    	}
    	
    	@Override
        public Bundle addingBundle(Bundle bundle, BundleEvent event) {    		
    		if (!bundle.equals(target))
                return null;
    		if (bundle.getState() == Bundle.ACTIVE) {    			
    			start();
    			return null;
    		}               	
    		return bundle;
        }

	    @Override
        public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle ignore) {	    	
	    	if (event.getType() == BundleEvent.STARTED) {
	    		start();
	    	}
	    }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event, Bundle ignore) {
        }
        
    }
}
