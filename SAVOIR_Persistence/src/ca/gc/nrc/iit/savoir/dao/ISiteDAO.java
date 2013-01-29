// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import java.util.List;

import ca.gc.iit.nrc.savoir.domain.Site;

public interface ISiteDAO{
	public void addSite(Site s);
	public void removeSite (int siteId);
	public Site getSiteById(int siteId);
	public List<Site> getSitesByValue(String location);
}
