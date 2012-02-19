/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.routing.preprocessing.graph;

import java.util.HashSet;

import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

/**
 * This class represents a relation with its members and give tags.
 * 
 * @author Michael Bartel
 * 
 */
public class CompleteRelation {

	RelationMember[] member;
	HashSet<KeyValuePair> tags;

	/**
	 * The Constructor to create an instance
	 * 
	 * @param member
	 *            The members (e.g. ways nodes) of the relation
	 * @param tags
	 *            The tags that exist for this relation
	 */
	public CompleteRelation(RelationMember[] member, HashSet<KeyValuePair> tags) {
		this.member = member;
		this.tags = tags;
	}

	/**
	 * @return all members of this relation
	 */
	public RelationMember[] getMember() {
		return member;
	}

	/**
	 * @return all tags for this relation
	 */
	public HashSet<KeyValuePair> getTags() {
		return tags;
	}

	@Override
	public String toString() {
		String s = "";
		s += " Member ";
		for (RelationMember rm : this.member) {
			s += rm.toString() + ", ";
		}

		s += " TAGS ";
		for (KeyValuePair kv : this.tags) {
			s += kv.toString() + ", ";
		}
		return s;
	}

}
