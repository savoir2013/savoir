// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.dao.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import ca.gc.iit.nrc.savoir.domain.types.Lookup;
import ca.gc.nrc.iit.savoir.dao.ITypesDAO;
import ca.gc.nrc.iit.savoir.utils.ResourceBundleFactory;

public class TestLookUpDAO extends BaseTestCase {

	private ITypesDAO dao;

	public void setUp() {
		super.setUp();
		dao = (ITypesDAO) super.getBeanManger().getContext().getBean(
				"parameterTypeDAO");
	}

	public void test() {
		try {
			Lookup parameterLookupUtil = new Lookup(dao);
			System.out
					.println("The parameter description is: "
							+ parameterLookupUtil
									.getDescription((String) ResourceBundleFactory
											.getProperty("parameterType",
													"SITE_NAME")));

			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);
			String line = in.readLine();

			System.out.println("The parameter description is: "
					+ parameterLookupUtil
							.getDescription((String) ResourceBundleFactory
									.getProperty("parameterType",
											"PLEORA_RX_IP_ADDRESS")));

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
