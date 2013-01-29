// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.server;

import javax.jws.WebService;

@WebService
public interface IRegServer {
    String QueryRegServer(String text);
}
