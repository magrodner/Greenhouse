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
package com.springsource.greenhouse.settings;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import com.springsource.greenhouse.account.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * UI Controller for changing member account settings.
 * @author Keith Donald
 */
@Controller
// TODO move the jdbc logic here into AppRepository
public class SettingsController {

	private JdbcTemplate jdbcTemplate;
	
	@Inject
	public SettingsController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Render the setting page to the member as HTML in their web browser.
	 * Puts the list of applications the member has connected their account with in the model (to support Disconnecting those apps if desired).
	 */
	@RequestMapping(value="/settings", method=RequestMethod.GET)
	public void settingsPage(Account account, Model model) {
		List<Map<String, Object>> apps = jdbcTemplate.queryForList("select a.name as name, c.accessToken from AppConnection c, App a where c.member = ? and c.app = a.id", account.getId());
		model.addAttribute("apps", apps);
	}
	
	/**
	 * Revoke the account access that was previously granted to a client application.
	 * After executing this operation, the client application will no longer be able to access or update the member's data.
	 * Used when the member no longer wishes to use the client application, or if the client application was compromised in some way (for example, if the user's mobile phone where the app
	 * was installed was stolen).
	 * @param accessToken the access token identifying the connection between a client application and a member account
	 * @param account the member requesting the termination of the access grant
	 * @return A redirect back to settings page.
	 */
	@RequestMapping(value="/settings/apps/{accessToken}", method=RequestMethod.DELETE)
	public String disconnectApp(@PathVariable String accessToken, Account account) {
		jdbcTemplate.update("delete from AppConnection where accessToken = ? and member = ?", accessToken, account.getId());
		return "redirect:/settings";
	}
}
