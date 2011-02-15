/*
 * Copyright 2010 the original author or authors.
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
package com.springsource.greenhouse.connect.providers;

import org.springframework.social.twitter.DuplicateTweetException;
import org.springframework.social.twitter.TwitterOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;

import com.springsource.greenhouse.account.Account;
import com.springsource.greenhouse.connect.ServiceProvider;
import com.springsource.greenhouse.connect.ConnectInterceptor;

/**
 * Supports posting a tweet on behalf of the user after connecting to Twitter.
 * @author Keith Donald
 */
public class TwitterConnectInterceptor implements ConnectInterceptor<TwitterOperations> {

	public void preConnect(ServiceProvider<TwitterOperations> provider, WebRequest request) {
		if (StringUtils.hasText(request.getParameter(POST_TWEET_PARAMETER))) {
			request.setAttribute(POST_TWEET_ATTRIBUTE, Boolean.TRUE, WebRequest.SCOPE_SESSION);
		}
	}

	public void postConnect(ServiceProvider<TwitterOperations> provider, Account account, WebRequest request) {
		if (request.getAttribute(POST_TWEET_ATTRIBUTE, WebRequest.SCOPE_SESSION) != null) {
			try {
				provider.getServiceOperations(account.getId()).updateStatus("Join me at the Greenhouse! " + account.getProfileUrl());
			} catch (DuplicateTweetException e) {
			}
			request.removeAttribute(POST_TWEET_ATTRIBUTE, WebRequest.SCOPE_SESSION);
		}
	}
	
	private static final String POST_TWEET_PARAMETER = "postTweet";
	
	private static final String POST_TWEET_ATTRIBUTE = "twitterConnect." + POST_TWEET_PARAMETER;

}