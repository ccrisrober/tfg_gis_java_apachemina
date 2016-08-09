// Copyright (c) 2015, maldicion069 (Cristian Rodríguez) <ccrisrober@gmail.con>
//
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.package com.example

package com.tfg.mina;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.json.simple.parser.ParseException;
import snaq.db.ConnectionPoolManager;
import snaq.db.DBPoolDataSource;

public class App {

    static List<Map> maps = new LinkedList<>();

    static DBPoolDataSource ds;
    static final String PollName = "MyPool";

    public static void main(String[] args) throws IOException, ParseException, SQLException {
        // https://code.google.com/p/json-simple/wiki/EncodingExamples#Example_1-1_-_Encode_a_JSON_object

        String dbUrl = "jdbc:mysql://localhost/tfg_gis";
        String username = "root";
        String password = "";

        ds = new DBPoolDataSource();
        ds.setName(PollName);
        ds.setDescription("Pooling DataSource");
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl(dbUrl);
        ds.setUser(username);
        ds.setPassword(password);
        ds.setMinPool(5);
        ds.setMaxPool(10);
        ds.setMaxSize(30);
        ds.setIdleTimeout(3600);  // Specified in seconds.

        PreparedStatement ps = null;
        ResultSet rs = null;
        java.util.Map<String, KeyObject> keys = new java.util.HashMap<>();
        KeyObject[] ks;
        ConnectionPoolManager cpm = null;
        try {
            Connection conn = ds.getConnection();
            ps = conn.prepareStatement("UPDATE `users` SET `isAlive`=0;");
            ps.executeUpdate();
            System.out.println("UPDATE");
            ps = conn.prepareStatement("SELECT o.color, o.id, om.posX, om.posY FROM object_map om INNER JOIN object o ON o.id = om.id_obj WHERE om.id_map=1");
            rs = ps.executeQuery();
            while (rs.next()) {
                keys.put(rs.getString("color"), new KeyObject(rs.getInt("id"), rs.getString("color"), rs.getFloat("posX"), rs.getFloat("posY")));
            }
            System.out.println("KEYS");
            KeyObject auxKey;
            rs.beforeFirst();
            List<KeyObject> ks_list = new LinkedList<>();
            while (rs.next()) {
                auxKey = keys.get(rs.getString("color"));
                if (auxKey != null) {
                    ks_list.add(auxKey);
                }
            }
            System.out.println("MAP");
            ks = new KeyObject[ks_list.size()];
            int i = 0;
            for (KeyObject ko : ks_list) {
                ks[i] = ko;
                i++;
            }
            ps = conn.prepareStatement("SELECT * FROM `map` WHERE `id`= 1;");
            rs = ps.executeQuery();
            while (rs.next()) {
                maps.add(new Map(rs.getInt("id"), rs.getString("mapFields"), rs.getInt("width"), rs.getInt("height"), ks));
                System.out.println("MAP --");
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        } finally {
            if (cpm != null) {
                cpm.release();
            }
        }

        KeyObject ko = new KeyObject(5, "Red", 5 * 64, 5 * 64);
        System.out.println(ko.toJSONString());

        boolean isGame = false;
        /*System.out.println("[S/s] Game Mode / [_] Test Mode");
         Scanner scanner = new Scanner(System.in);
         String opc = scanner.next().toLowerCase();
         if(opc.compareToIgnoreCase("s") == 0) {
         isGame = true;
         }*/

        try {
            IoAcceptor acceptor = new NioSocketAcceptor();
            acceptor.getFilterChain().addLast("logger", new LoggingFilter());
            acceptor.getFilterChain().addLast(
                    "codec",
                    new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
            acceptor.setHandler(new MyServerHandler(isGame));
            acceptor.getSessionConfig().setReadBufferSize(2048);
            System.out.println("INIT");
            acceptor.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 8090));
        } catch (Exception e) {
        }

    }
}
