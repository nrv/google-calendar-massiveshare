/*
 * Copyright 2013 Nicolas HERVE.
 * 
 * This file is part of google-calendar-massiveshare
 * 
 * google-calendar-massiveshare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * google-calendar-massiveshare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with google-calendar-massiveshare. If not, see <http://www.gnu.org/licenses/>.
 */
package name.herve.gcms;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.extensions.java6.auth.oauth2.GooglePromptReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Acl;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

/**
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class CalendarWrapper {
	private static final String APPLICATION_NAME = "google-calendar-massiveshare";
	private JsonFactory jsonFactory;
	private HttpTransport httpTransport;
	private Calendar client;

	private Credential authorize() throws Exception {
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(getClass().getResourceAsStream("/client_secrets.json")));
		if (clientSecrets.getDetails().getClientId().startsWith("Enter") || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=plus " + "into plus-cmdline-sample/src/main/resources/client_secrets.json");
			System.exit(1);
		}
		FileCredentialStore credentialStore = new FileCredentialStore(new File(System.getProperty("user.home"), ".credentials/calendar.json"), jsonFactory);
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR)).setCredentialStore(credentialStore).build();
		return new AuthorizationCodeInstalledApp(flow, new GooglePromptReceiver()).authorize("user");
	}

	public void init() throws IOException {
		try {
			jsonFactory = new JacksonFactory();
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			Credential credential = authorize();
			client = new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public AccessList getAccessList(String calendar) throws IOException {
		AccessList result = new AccessList();

		Acl acls = client.acl().list(calendar).execute();

		for (AclRule r : acls.getItems()) {
			result.set(r.getScope().getValue(), AccessList.Role.valueOf(r.getRole()), AccessList.ScopeType.valueOf(r.getScope().getType()), r.getId());
		}

		return result;
	}

	public void updateAccessList(String calendar, AccessList currentList, AccessList newList, ACLModifier mod) throws IOException {
		// delete & update
		for (String email : currentList) {
			if (newList.containsEmail(email)) {
				if (currentList.getRight(email) != newList.getRight(email) || currentList.getType(email) != newList.getType(email)) {
					mod.update(client, calendar, currentList.getId(email), email, newList.getRight(email), newList.getType(email));
				}
			} else {
				mod.delete(client, calendar, currentList.getId(email));
			}
		}

		// insert
		for (String email : newList) {
			if (!currentList.containsEmail(email)) {
				mod.insert(client, calendar, email, newList.getRight(email), newList.getType(email));
			}
		}
	}

	public List<Map<String, String>> getCalendars() throws IOException {
		ArrayList<Map<String, String>> cals = new ArrayList<Map<String, String>>();

		CalendarList feed = client.calendarList().list().execute();
		if (feed.getItems() != null) {
			for (CalendarListEntry entry : feed.getItems()) {
				String ar = entry.getAccessRole();
				if (ar.equals("owner") || ar.equals("writer")) {
					Map<String, String> nc = new HashMap<String, String>();
					nc.put("id", entry.getId());
					nc.put("summary", entry.getSummary());
					nc.put("description", entry.getDescription());
					cals.add(nc);
				}
			}
		}
		return cals;
	}
}
