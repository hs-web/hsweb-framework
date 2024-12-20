package org.hswebframework.web.crud.generator;

public interface Generators {

    /**
     * @see DefaultIdGenerator
     */
    String DEFAULT_ID_GENERATOR = "default_id";


    /**
     * @see MD5Generator
     */
    String MD5 = "md5";

    /**
     * @see SnowFlakeStringIdGenerator
     */
    String SNOW_FLAKE = "snow_flake";

    /**
     * @see CurrentTimeGenerator
     */
    String CURRENT_TIME = "current_time";

    /**
     * @see org.hswebframework.web.id.RandomIdGenerator
     */
    String RANDOM = "random";

}
