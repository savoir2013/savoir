// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao;

import ca.gc.iit.nrc.savoir.domain.Term;

public interface ITermDAO{
	public void addTerm(Term t);
	public void updateTerm(int termId);
	public void removeTerm (int termId);
	public Term getTermById(int termId);
	public Term getTermByName(String termName);
}
