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

import java.io.IOException;

import name.herve.gcms.AccessList.Role;
import name.herve.gcms.AccessList.ScopeType;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;

/**
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class ACLModifier {
	public ACLModifier() {
		super();
		setDebug(true);
	}

	private boolean debug;

	public void insert(Calendar client, String calendar, String email, Role role, ScopeType type) throws IOException {
		System.out.println("[" + calendar + "]   insert(" + email + ", " + role + ", " + type + ")");
		if (!isDebug()) {
			AclRule nr = new AclRule();
			nr.setRole(role.toString());
			nr.setScope(new Scope().setType(type.toString()).setValue(email));
			client.acl().insert(calendar, nr).execute();
		}
	}

	public void delete(Calendar client, String calendar, String id) throws IOException {
		System.out.println("[" + calendar + "]   delete(" + id + ")");
		if (!isDebug()) {
			client.acl().delete(calendar, id).execute();
		}
	}

	public void update(Calendar client, String calendar, String id, String email, Role role, ScopeType type) throws IOException {
		System.out.println("[" + calendar + "]   update(" + id + ", " + email + ", " + role + ", " + type + ")");
		if (id == null) {
			throw new IOException("id == null");
		}
		if (!isDebug()) {
			AclRule nr = new AclRule();
			nr.setRole(role.toString());
			nr.setScope(new Scope().setType(type.toString()).setValue(email));
			client.acl().update(calendar, id, nr).execute();
		}
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
