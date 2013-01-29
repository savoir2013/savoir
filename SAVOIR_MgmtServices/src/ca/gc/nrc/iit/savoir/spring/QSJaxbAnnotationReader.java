// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.spring;

import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;

/**
 * Patched version of JAXB's RuntimeAnnotationReader that marks java.lang.Exception as @XmlTransient
 */
public class QSJaxbAnnotationReader extends TransientAnnotationReader implements RuntimeAnnotationReader {

    public QSJaxbAnnotationReader () {
        try {

            addTransientField(Throwable.class.getDeclaredField("stackTrace"));
            addTransientMethod(Throwable.class.getDeclaredMethod("getStackTrace"));

        } catch (NoSuchMethodException unexpected) {
            throw new RuntimeException (unexpected);
        } catch (NoSuchFieldException unexpected) {
            throw new RuntimeException (unexpected);
        }
    }
}
