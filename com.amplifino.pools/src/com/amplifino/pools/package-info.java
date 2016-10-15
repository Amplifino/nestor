/**
 * Generic Object Pool
 * 
 * The current default implementation has several possible strategies for removing idle members that exceed the maxIdleTimeout threshold.
 * 
 *  - remove lazily when a pool user tries to borrow a member and hits an expired entry. Note that this may cause a small hiccup in the application.
 *  - configure the pool to execute cycle() at regular intervals using an internal SchedulerExecutorService
 *  - if you have lots of pools, you may prefer to configure all the pools with the same external SchedulerExecutorService
 *  - or call cycle() at regular intervals by an external scheduler.
 *  
 * It also defaults to a LIFO (Last in, First Out) allocation strategy.
 * This typically results in better cache hits   
 */
@Version("1.2.0")
package com.amplifino.pools;

import org.osgi.annotation.versioning.Version;
