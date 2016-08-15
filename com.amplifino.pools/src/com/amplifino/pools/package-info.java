/**
 * Generic Object Pool
 * 
 * The current default implementation has a lazy strategy for removing idle members that exceed the maxIdleTimeout threshold.
 * ( These will be removed when a pool user tries to borrow a member).
 * It also defaults to a LIFO (Last in, First Out) allocation strategy.
 * This typically results in better cache hits   
 */
@Version("1.2.0")
package com.amplifino.pools;

import org.osgi.annotation.versioning.Version;
