/*
 * Copyright 2008 Google, Inc.
 *
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
package ca.gc.nrc.iit.oauth.provider.validator;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import ca.gc.nrc.iit.oauth.common.OAuth;
import ca.gc.nrc.iit.oauth.common.OAuthConsumer;
import ca.gc.nrc.iit.oauth.common.OAuthParams;
import ca.gc.nrc.iit.oauth.common.OAuthToken;
import ca.gc.nrc.iit.oauth.common.exception.OAuthException;
import ca.gc.nrc.iit.oauth.common.exception.OAuthProblemException;
import ca.gc.nrc.iit.oauth.common.signature.OAuthSignatureMethod;


/**
 * A simple OAuthValidator, which checks the version, whether the timestamp is
 * close to now, the nonce hasn't been used before and the signature is valid.
 * Each check may be overridden.
 * <p>
 * This implementation is less than industrial strength:
 * <ul>
 * <li>Duplicate nonces won't be reliably detected by a service provider running
 * in multiple processes, since the used nonces are stored in memory.</li>
 * <li>The collection of used nonces is a synchronized choke point</li>
 * <li>The used nonces may occupy lots of memory, although you can minimize this
 * by calling releaseGarbage periodically.</li>
 * <li>The range of acceptable timestamps can't be changed, and there's no
 * system for increasing the range smoothly.</li>
 * <li>Correcting the clock backward may allow duplicate nonces.</li>
 * </ul>
 * For a big service provider, it might be better to store used nonces in a
 * database.
 * 
 * @author Dirk Balfanz, Google
 * @author John Kristian, Netflix
 * @author Aaron Moss, NRC-IIT
 */
public class SimpleOAuthValidator implements OAuthValidator, TimestampValidator {

    /** The default maximum age of timestamps is 5 minutes. */
    public static final long DEFAULT_MAX_TIMESTAMP_AGE = 5 * 60 * 1000L;
    public static final long DEFAULT_TIMESTAMP_WINDOW = DEFAULT_MAX_TIMESTAMP_AGE;

    /**
     * Construct a validator that rejects messages more than five minutes old or
     * with a OAuth version other than 1.0.
     */
    public SimpleOAuthValidator() {
        this(DEFAULT_TIMESTAMP_WINDOW, OAuth.VERSION_1_0);
    }

    /**
     * Public constructor.
     * 
     * @param maxTimestampAgeMsec
     *            the range of valid timestamps, in milliseconds into the past
     *            or future. So the total range of valid timestamps is twice
     *            this value, rounded to the nearest second.
     * @param maxVersion
     *            the maximum valid oauth_version
     */
    public SimpleOAuthValidator(long maxTimestampAgeMsec, double maxVersion) {
        this.maxTimestampAgeMsec = maxTimestampAgeMsec;
        this.maxVersion = maxVersion;
        this.timestampValidator = this;
    }

    protected final double minVersion = 1.0;
    protected final double maxVersion;
    protected long maxTimestampAgeMsec;
    protected TimestampValidator timestampValidator;
    private final Set<UsedNonce> usedNonces = new TreeSet<UsedNonce>();
    

    /**
     * Allow objects that are no longer useful to become garbage.
     * 
     * @return the earliest point in time at which another call will release
     *         some garbage, or null to indicate there's nothing currently
     *         stored that will become garbage in future. This value may change,
     *         each time releaseGarbage or validateNonce is called.
     */
    public Date releaseGarbage() {
        return removeOldNonces(currentTimeMsec());
    }

    /**
     * Remove usedNonces with timestamps that are too old to be valid.
     */
    private Date removeOldNonces(long currentTimeMsec) {
        UsedNonce next = null;
        UsedNonce min = new UsedNonce((currentTimeMsec - maxTimestampAgeMsec + 500) / 1000L);
        synchronized (usedNonces) {
            // Because usedNonces is a TreeSet, its iterator produces
            // elements from oldest to newest (their natural order).
            for (Iterator<UsedNonce> iter = usedNonces.iterator(); iter.hasNext();) {
                UsedNonce used = iter.next();
                if (min.compareTo(used) <= 0) {
                    next = used;
                    break; // all the rest are also new enough
                }
                iter.remove(); // too old
            }
        }
        if (next == null)
            return null;
        return new Date((next.getTimestamp() * 1000L) + maxTimestampAgeMsec + 500);
    }

    /** {@inherit} 
     */
    public void validateMessage(OAuthParams params, OAuthToken token, OAuthConsumer consumer)
    	throws OAuthException {
        validateVersion(params);
        validateTimestampAndNonce(params);
        validateSignature(params, token, consumer);
    }
    
    public void setMaxTimestampAgeMsec(long maxTimestampAgeMsec) {
    	this.maxTimestampAgeMsec = maxTimestampAgeMsec;
    }
    
    public void setTimestampValidator(TimestampValidator timestampValidator) {
    	this.timestampValidator = timestampValidator;
    }

    protected void validateVersion(OAuthParams params)
    		throws OAuthException {
        double version = params.getVersion();
        if (version < minVersion || maxVersion < version) {
            OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.VERSION_REJECTED);
            problem.setParameter(OAuth.Problems.OAUTH_ACCEPTABLE_VERSIONS, minVersion + "-" + maxVersion);
            throw problem;
        }
    }

    /**
     * Throw an exception if the timestamp is out of range or the nonce has been
     * validated previously.
     */
    protected void validateTimestampAndNonce(OAuthParams params) throws OAuthProblemException {
        long now = currentTimeMsec();
        timestampValidator.validateTimestamp(params, now);
        validateNonce(params, now);
    }

    /** Throw an exception if the timestamp [sec] is out of range. */
    public void validateTimestamp(OAuthParams params, long currentTimeMsec) 
    		throws OAuthProblemException {
        long timestamp = params.getTimestamp();
    	long min = (currentTimeMsec - maxTimestampAgeMsec + 500) / 1000L;
        long max = (currentTimeMsec + maxTimestampAgeMsec + 500) / 1000L;
        if (timestamp < min || max < timestamp) {
            OAuthProblemException problem = new OAuthProblemException(OAuth.Problems.TIMESTAMP_REFUSED);
            problem.setParameter(OAuth.Problems.OAUTH_ACCEPTABLE_TIMESTAMPS, min + "-" + max);
            throw problem;
        }
    }

    /**
     * Throw an exception if the nonce has been validated previously.
     * 
     * @return the earliest point in time at which a call to releaseGarbage
     *         will actually release some garbage, or null to indicate there's
     *         nothing currently stored that will become garbage in future.
     */
    protected Date validateNonce(OAuthParams params, long currentTimeMsec) 
    		throws OAuthProblemException {
        long timestamp = params.getTimestamp();
    	UsedNonce nonce = new UsedNonce(timestamp, params.getNonce(), 
        		params.getConsumerKey(), params.getToken());
        /*
         * The OAuth standard requires the token to be omitted from the stored
         * nonce. But I include it, to harmonize with a Consumer that generates
         * nonces using several independent computers, each with its own token.
         */
        boolean valid = false;
        synchronized (usedNonces) {
            valid = usedNonces.add(nonce);
        }
        if (!valid) {
            throw new OAuthProblemException(OAuth.Problems.NONCE_USED);
        }
        return removeOldNonces(currentTimeMsec);
    }

    protected void validateSignature(OAuthParams params, OAuthToken token, OAuthConsumer consumer)
    		throws OAuthException {
    	OAuthSignatureMethod.newMethod(params.getSignatureMethod())
    		.validate(params, token, consumer);
    }

    /** Get the number of milliseconds since midnight, January 1, 1970 UTC. */
    protected long currentTimeMsec() {
        return System.currentTimeMillis();
    }

    /**
     * Selected parameters from an OAuth request, in a form suitable for
     * detecting duplicate requests. The implementation is optimized for the
     * comparison operations (compareTo, equals and hashCode).
     * 
     * @author John Kristian
     */
    private static class UsedNonce implements Comparable<UsedNonce> {
        /**
         * Construct an object containing the given timestamp, nonce and other
         * parameters. The order of parameters is significant.
         */
        UsedNonce(long timestamp, String... nonceEtc) {
            StringBuilder key = new StringBuilder(
            		String.format("%20d", Long.valueOf(timestamp)));
            // The blank padding ensures that timestamps are compared as numbers.
            for (String etc : nonceEtc) {
                key.append("&").append(etc == null ? " " : OAuth.percentEncode(etc));
                // A null value is different from "" or any other String.
            }
            sortKey = key.toString();
        }

        private final String sortKey;

        long getTimestamp() {
            int end = sortKey.indexOf("&");
            if (end < 0)
                end = sortKey.length();
            return Long.parseLong(sortKey.substring(0, end).trim());
        }

        /**
         * Determine the relative order of <code>this</code> and
         * <code>that</code>, as specified by Comparable. The timestamp is most
         * significant; that is, if the timestamps are different, return 1 or
         * -1. If <code>this</code> contains only a timestamp (with no nonce
         * etc.), return -1 or 0. The treatment of the nonce etc. is murky,
         * although 0 is returned only if they're all equal.
         */
        public int compareTo(UsedNonce that) {
            return (that == null) ? 1 : sortKey.compareTo(that.sortKey);
        }

        @Override
        public int hashCode() {
            return sortKey.hashCode();
        }

        /**
         * Return true iff <code>this</code> and <code>that</code> contain equal
         * timestamps, nonce etc., in the same order.
         */
        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that == this)
                return true;
            if (that.getClass() != getClass())
                return false;
            return sortKey.equals(((UsedNonce) that).sortKey);
        }

        @Override
        public String toString() {
            return sortKey;
        }
    }
}
