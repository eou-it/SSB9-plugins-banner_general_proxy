/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.converters

import net.hedtech.restfulapi.config.RepresentationConfig
import org.apache.log4j.Logger

class JobsubOutputMarshaller {
    private static final log = Logger.getLogger(JobsubOutputMarshaller.class)

    public void init() {
        log.trace("jobsubOutputMarshaller initialized");
    }

    InputStream marshalObject(Object o, RepresentationConfig config) {
        InputStream result;

        if (o instanceof InputStream) {
            result = (InputStream) o
        }
        else {
            throw new UnsupportedOperationException("only input streams can be marshalled")
        }

        return result
    }
}
