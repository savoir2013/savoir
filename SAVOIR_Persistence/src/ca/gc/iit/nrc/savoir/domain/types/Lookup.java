// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.iit.nrc.savoir.domain.types;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.gc.nrc.iit.savoir.dao.ITypesDAO;

public class Lookup {

	public List<? extends Lookupable> all; 
	public Map<String, Lookupable> map;

	public Lookup(ITypesDAO dao) throws SQLException {
//		map = new HashMap<String, Lookupable>();
//		all = dao.getAll();
//		for (Lookupable element : all) {
//			map.put(element.getId(), element);
//			System.out.println(element.getId());			
//		}
	}

	public String getName(String item) {
		return map.get(item).getName();
	}

	public String getId(String item) {
		return map.get(item).getId();
	}

	public String getDescription(String item) {
		return map.get(item).getDescription();
	}

}
