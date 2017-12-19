package org.hswebframework.web.database.manager.meta.table.parser;

import org.hswebframework.web.database.manager.meta.ObjectMetadata;

import java.sql.SQLException;
import java.util.List;

public interface MetaDataParser<M extends ObjectMetadata> {

    List<M> parseAll() throws SQLException;

    M parse(String objectName) throws SQLException;
}
