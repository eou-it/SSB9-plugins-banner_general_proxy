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

        if (o instanceof Collection) {
            throw new UnsupportedOperationException("don't Marshal Collections please")
        }
        else {
            result = (InputStream) o
        }
        return result
    }
}
