/*
 * ModeShape (http://www.modeshape.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modeshape.connector.meta.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * The Microsoft SQL Server JDBC drivers return a list of users from the {@link java.sql.DatabaseMetaData#getSchemas()} method instead of
 * the actual schemas. Unfortunately, the {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])} method actually
 * returns schema names, so the default {@link JdbcMetadataCollector} implementation doesn't match up correctly. This class should
 * be used when the Microsoft JDBC driver is used for database connectivity. The jTDS driver has already corrected this bug and
 * can use the default {@link JdbcMetadataCollector}.
 */
public class SqlServerMetadataCollector extends JdbcMetadataCollector {

    @Override
    public List<String> getSchemaNames( Connection conn,
                                        String catalogName ) throws JdbcMetadataException {
        Statement stmt = null;
        ResultSet rs = null;
        List<String> schemaNames = new LinkedList<String>();

        try {
            stmt = conn.createStatement();

            // There's no correlation between schemas and catalogs in MS SQL Server, so return all schemas
            rs = stmt.executeQuery("SELECT name AS TABLE_SCHEM FROM sys.schemas ORDER BY TABLE_SCHEM");
            while (rs.next()) {
                schemaNames.add(rs.getString("TABLE_SCHEM"));
            }

            return schemaNames;
        } catch (SQLException se) {
            throw new JdbcMetadataException(se);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignore) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ignore) {
            }
        }
    }

}
