# com.amplifino.nestor.security.http #

Connects Basic and Digest Authentication to OSGI User Admin service.
The default configuration only protect urls matching /api/*
Can be changed by Config Admin

Assumes user has a HA1 credential, corresponding to the password hash used in Digest authentication.
HA1 = MD5(username:realm:password) base64 encoded
 


