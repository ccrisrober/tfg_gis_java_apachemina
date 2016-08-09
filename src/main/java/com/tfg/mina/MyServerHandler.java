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

import static com.tfg.mina.App.PollName;
import static com.tfg.mina.App.ds;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.tfg.mina.FightStatus.Status;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import snaq.db.ConnectionPoolManager;

@SuppressWarnings("unchecked")
public class MyServerHandler extends IoHandlerAdapter {

    protected static java.util.Map<Long, String> users_sockets = new java.util.concurrent.ConcurrentHashMap<>();
    protected static java.util.Map<Long, IoSession> sockets = new java.util.concurrent.ConcurrentHashMap<>();
    protected static java.util.Map<Integer, FightStatus> fights = new java.util.concurrent.ConcurrentHashMap<>();
    protected static AtomicInteger countFight = new AtomicInteger(1);
    //protected static AtomicInteger ids = new AtomicInteger(1);

    protected static JSONParser parser = new JSONParser();

    protected static JSONObject addObjToClient = new JSONObject();
    protected static JSONObject exitToClient = new JSONObject();
    protected static JSONObject newUserToAnotherClients = new JSONObject();
    protected static JSONObject sendNewMap = new JSONObject();
    protected static JSONObject sendFinishBattle = new JSONObject();
    protected static JSONObject sendHide = new JSONObject();
    protected static JSONObject sendFight = new JSONObject();
    protected static Random rand;
    protected boolean isGame;

    static {
        rand = new Random();
        addObjToClient.put("Action", "addObj");
        exitToClient.put("Action", "exit");
        newUserToAnotherClients.put("Action", "new");
        sendNewMap.put("Action", "sendMap");
        sendFinishBattle.put("Action", "finishBattle");
        sendHide.put("Action", "hide");
        sendFight.put("Action", "fight");
    }

    protected static String getAddObjToClient(KeyObject ko) {
        addObjToClient.put("Obj", ko);
        return addObjToClient.toJSONString();
    }

    protected static String getExitToClient(long id) {
        exitToClient.put("Id", id);
        return exitToClient.toJSONString();
    }

    protected static String getNewToClient(long id, float px, float py) {
        newUserToAnotherClients.put("Id", id);
        newUserToAnotherClients.put("PosX", px);
        newUserToAnotherClients.put("PosY", py);
        return newUserToAnotherClients.toJSONString();
    }

    protected static String getSendMap(ObjectUser ou) throws SQLException {
        sendNewMap.put("Map", App.maps.get(ou.getMap()));
        sendNewMap.put("X", 5 * 64);
        sendNewMap.put("Y", 5 * 64);
        sendNewMap.put("Id", ou.getId());

        PreparedStatement ps = null;
        ResultSet rs = null;

        java.util.Map<String, ObjectUser> positions = new java.util.HashMap<>();
        java.util.Map<String, KeyObject> objects = new java.util.HashMap<>();
        try {
            Connection conn = App.ds.getConnection();

            ps = conn.prepareStatement("SELECT `port`, `posX`, `posY` FROM `users` WHERE `isAlive`=1;");
            rs = ps.executeQuery();
            while (rs.next()) {
                positions.put(rs.getInt("port") + "", new ObjectUser(rs.getInt("port"), rs.getFloat("posX"), rs.getFloat("posY")));
            }

            String name = users_sockets.get(ou.getId());
            ps = conn.prepareStatement("SELECT * FROM `object_map` WHERE `admin`='" + name + "' AND `id_map`=1;");
            rs = ps.executeQuery();
            while (rs.next()) {
                objects.put(rs.getInt("port") + "", new KeyObject(rs.getInt("id"), rs.getString("color"), rs.getFloat("posX"), rs.getFloat("posY")));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
        sendNewMap.put("Users", positions);
        sendNewMap.put("Objects", objects);
        return sendNewMap.toJSONString();
    }

    protected static String getFinishBattle(int valueC, int valueE, long winner) {
        sendFinishBattle.put("ValueClient", valueC);
        sendFinishBattle.put("ValueEnemy", valueE);
        sendFinishBattle.put("Winner", winner);
        return sendFinishBattle.toJSONString();
    }

    protected static String getHide(long player1, long player2) {
        long[] player = {player1, player2};
        sendHide.put("Ids", player);
        return sendHide.toJSONString();
    }

    protected static String getFight(long enemy_id) {
        sendFight.put("Id_enemy", enemy_id);
        return sendFight.toJSONString();
    }

    protected static int randRange(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    public MyServerHandler(boolean isGame) {
        this.isGame = isGame;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        //cause.printStackTrace();
    }

    protected float getFloatFromObject(Object o) throws Exception {
        return Float.parseFloat(o.toString());
    }

    protected void sendDiePlayerAndWinnerToShow(IoSession session, long emisor_id, long receiver_id) throws SQLException {
        Integer emisor_roll = null, receiver_roll = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = App.ds.getConnection();
            ps = conn.prepareStatement("SELECT `port`, `rollDice` FROM `users` WHERE `port`=" + emisor_id + " or `port`=" + receiver_id + ";");
            rs = ps.executeQuery();
            emisor_roll = null;
            receiver_roll = null;
            while (rs.next()) {
                if (rs.getLong("port") == emisor_roll) {
                    emisor_roll = rs.getInt("rollDice");
                } else if (rs.getLong("port") == receiver_roll) {
                    receiver_roll = rs.getInt("rollDice");
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        long winner = -1;
        int valueC = -1, valueE = -1;
        if (receiver_roll == null) {
            winner = emisor_id;
            valueE = emisor_roll;
        } else if (emisor_roll == null) {
            winner = receiver_id;
            valueC = receiver_roll;
        } else if (emisor_roll > receiver_roll) {
            winner = emisor_id;
            valueE = emisor_roll;
            valueC = receiver_roll;
        } else if (emisor_roll < receiver_roll) {
            winner = receiver_id;
            valueE = emisor_roll;
            valueC = receiver_roll;
        }
        session.write(getFinishBattle(valueC, valueE, winner));
    }

    protected static void sendFightToAnotherClient(long emisor_id, long receiver_id) throws SQLException {
        // Save die roll value from emisor_id
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = App.ds.getConnection();
            ps = conn.prepareStatement("UPDATE `users` SET `rollDice`=" + randRange(1, 6) + "; WHERE `port`=" + emisor_id + ";");
            ps.executeUpdate();
            ps.close();

            // Save die roll value from receiver_id
            ps = conn.prepareStatement("UPDATE `users` SET `rollDice`=" + randRange(1, 6) + "; WHERE `port`=" + receiver_id + ";");
            ps.executeUpdate();
            ps.close();

            new SendFightThread(emisor_id, receiver_id, getHide(emisor_id, receiver_id));
        } catch (SQLException ex) {
            System.out.println(ex);
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        long port = session.getId();
        //System.out.println(port);
        // session.getRemoteAddress().
        String str = message.toString();
        System.out.println(str);
        try {
            JSONObject data = (JSONObject) parser.parse(str);
            Object aux = data.get("Action");
            if (aux != null) {
                String action = (String) aux;
                //System.out.println(action);
                if (action != null) {
                    if (action.equals("initWName")) {
                        boolean insert = true;  // true: ins, false: upd
                        float posX = 320;
                        float posY = 320;
                        String username = data.get("Name").toString();
                        PreparedStatement ps = null;
                        ResultSet rs = null;
                        Connection conn = null;
                        try {
                            conn = App.ds.getConnection();
                            ps = conn.prepareStatement("SELECT `port`, `posX`, `posY` FROM `users` WHERE `username`='" + username + "';");
                            //ps.setString(1, username);
                            rs = ps.executeQuery();
                            if (rs.next()) {
                                insert = false;
                                posX = rs.getFloat("posX");
                                posY = rs.getFloat("posY");
                            }
                            String str_;
                            if (insert) {	// Update
                                str_ = "INSERT INTO `users` (`port`, `username`) VALUES ('" + port + "', '" + username + "');";
                            } else {		// Insert
                                str_ = "UPDATE `users` SET `port`=" + port + ", `isAlive`=1 WHERE `username`='" + username + "';";
                            }
                            ps = conn.prepareStatement(str_);
                            System.out.println("Añadido/Cargado usuario");
                            //ps.setInt(1, port);
                            //ps.setString(2, username);
                            ps.executeUpdate();
                            ps.close();
                            sockets.put(port, session);
                            ObjectUser ou = new ObjectUser(port, posX, posY);

                            // Guardamos en positions
                            users_sockets.put(port, username);

                            // Mandamos el mapa
                            session.write(getSendMap(ou));
                            //System.out.println(getSendMap(ou));

                            if (isGame) {
                                //System.err.println("AVISANDO AL RESTO " + users_sockets.size());
                                //new SendAnothersThread(session.getId(), getNewToClient(ou.getId(), ou.getPosX(), ou.getPosY())).start();

                                String msg = getNewToClient(ou.getId(), ou.getPosX(), ou.getPosY());
                                new Thread(() -> {
                                    MyServerHandler.sockets.values().forEach(ko -> {
                                        System.out.println(session.getId() + " - " + ko.getId() + " => " + getNewToClient(ou.getId(), ou.getPosX(), ou.getPosY()));
                                        if (session.getId() != ko.getId()) {
                                            ko.write(msg);
                                        }
                                    });
                                }).start();
                            }
                        } catch (SQLException ex) {
                            System.out.println(ex);
                        } finally {
                            if (rs != null) {
                                rs.close();
                            }
                            if (ps != null) {
                                ps.close();
                            }
                            if (conn != null) {
                                conn.close();
                            }
                        }
                        return;
                    } else if (action.equals("move")) {
                        //new Thread(() -> {
                        PreparedStatement ps = null;
                        Connection conn = null;
                        try {
                            conn = App.ds.getConnection();
                            ps = conn.prepareStatement("UPDATE `users` SET `posX`=?,`posY`=? WHERE `port`=?;");
                            ps.setFloat(1, getFloatFromObject(((JSONObject) data.get("Pos")).get("X")));
                            ps.setFloat(2, getFloatFromObject(((JSONObject) data.get("Pos")).get("Y")));
                            ps.setLong(3, port);
                            if (ps.execute()) {
                                System.out.println("OK");
                            }

                            if (!isGame) {
                                session.write(str);
                            }
                        } catch (SQLException ex) {
                            System.out.println(ex);
                        } finally {
                            if (ps != null) {
                                ps.close();
                            }
                            if (conn != null) {
                                conn.close();
                            }
                        }
                        //}).start();
                    } else if (action.equals("position")) {
                        ObjectUser ou;
                        PreparedStatement ps = null;
                        ResultSet rs = null;
                        try {
                            Connection conn = App.ds.getConnection();
                            ps = conn.prepareStatement("SELECT `posX`, `posY` FROM `users` WHERE `port`=" + port + ";");
                            rs = ps.executeQuery();
                            ou = null;
                            while (rs.next()) {
                                ou = new ObjectUser(port, rs.getFloat("posX"), rs.getFloat("posY"));
                            }
                            if (ou != null) {
                                session.write(ou.toJSONString());
                            }
                        } catch (SQLException e) {
                        } finally {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (SQLException e) {
                                }
                            }
                            if (rs != null) {
                                try {
                                    rs.close();
                                } catch (SQLException e) {
                                }
                            }
                        }
                        return;

                        // NUEVOS EVENTOS
                    } else if (action.equals("initFight")) {

                        int id_battle = MyServerHandler.countFight.incrementAndGet();
                        int id_enemy = Integer.parseInt(data.get("Id_enemy").toString());
                        FightStatus fs = new FightStatus(id_battle, port, id_enemy);
                        MyServerHandler.fights.put(id_battle, fs);
                        // Enviamos el mensaje "sendInitFight" al otro usuario
                        MyServerHandler.sockets.get(id_enemy).write("{\"Action\":\"sendInitFight\",\"Id_battle\":" + id_battle + "}");
                        fs.setState(Status.NeedACK);
                        MyServerHandler.fights.put(id_battle, fs);

                    } else if (action.equals("sendAckInitFight")) {

                        int id_battle = Integer.parseInt(data.get("Id_battle").toString());
                        FightStatus fs = MyServerHandler.fights.get(id_battle);
                        fs.setState(Status.OKInit);
                        MyServerHandler.fights.put(id_battle, fs);

                        String msg = "{\"Action\":\"okInitFight\",\"Id_battle\":" + id_battle + "}";
                        // Para evitar problemas de emisor y receptor con el puerto, consulto directamente el mapa
                        MyServerHandler.sockets.get(fs.getId_emisor()).write(msg);
                        MyServerHandler.sockets.get(fs.getId_receiver()).write(msg);

                        fs.setState(Status.OKInit);
                        MyServerHandler.fights.put(id_battle, fs);

                    } else if (action.equals("okInitFight")) {

                        int id_battle = Integer.parseInt(data.get("Id_battle").toString());
                        FightStatus fs = MyServerHandler.fights.get(id_battle);
                        fs.addPlayer();
                        if (fs.getPlayers() == 2) {
                            fs.setState(Status.Fight);
                            MyServerHandler.fights.put(id_battle, fs);

                            // TODO: Me queda avisar ocultamiento de jugadores
                            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                            executorService.schedule(new FightRandomEventThread(fs), randRange(20, 40), TimeUnit.SECONDS);
                        }
                        MyServerHandler.fights.put(id_battle, fs);

                        // / NUEVOS EVENTOS
                    } else if (action.equals("getObj")) {
                        int id_obj = Integer.parseInt(data.get("Id_obj").toString());
                        //int id_user = Integer.parseInt(data.get("Id_user").toString());
						/*ObjectUser ou = MyServerHandler.positions.get(id_obj);
                         data.put("Action", "remObj");
                         KeyObject ko = App.maps.get(ou.getMap()).removeObject(id_obj);
                         ou.addObj(ko.getId());
                         str = data.toJSONString();

                         PreparedStatement ps = App.conn.prepareStatement("INSERT INTO `user_objects` (`id_obj`, `id_user`) VALUES ('?', '?');");
                         ps.setInt(1, ou.getId());
                         ps.setInt(2, port);

                         if (ps.execute()) {
                         System.out.println("OK");
                         }
                         ps.close();*/

                    } else if (action.equals("freeObj")) {
                        /*int id_obj = Integer.parseInt(((JSONObject)data.get("Obj")).get("Id_obj").toString());
                         //int id_user = Integer.parseInt(data.get("Id_user").toString());
                         float posX = Float.parseFloat(((JSONObject)data.get("Obj")).get("PosX").toString());
                         float posY = Float.parseFloat(((JSONObject)data.get("Obj")).get("PosY").toString());
                         ObjectUser ou = MyServerHandler.positions.get(id_obj);
                         KeyObject ko = App.maps.get(ou.getMap()).addObject(id_obj, posX, posY);
                         ou.remObj(ko.getId());
                         MyServerHandler.positions.put(ou.getId(), ou);
                         str = getAddObjToClient(ko);*/
                    } else if (action.equals("exit")) {
                        PreparedStatement ps = null;
                        Connection conn = null;
                        try {
                            conn = App.ds.getConnection();
                            ps = conn.prepareStatement("UPDATE `users` SET `isAlive`=0 WHERE `username`='?';");
                            ps.setString(1, users_sockets.get(port));
                            ps.executeUpdate();
                        } catch (SQLException ex) {
                            System.out.println(ex);
                        } finally {
                            if (ps != null) {
                                ps.close();
                            }
                            if (conn != null) {
                                conn.close();
                            }
                        }
                        MyServerHandler.users_sockets.remove(port);
                        MyServerHandler.sockets.remove(port);
                        if (!isGame) {
                            session.write(str);
                        } else {
                            str = getExitToClient(port);
                        }
                    }
                    if (isGame) {
                        new SendAnothersThread(session.getId(), str).start();
                    }
                }
            }
        } catch (ParseException | SQLException | NumberFormatException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    static class FightRandomEventThread extends Thread {

        FightStatus fs;

        public FightRandomEventThread(FightStatus fs) {
            this.fs = fs;
        }

        @Override
        public void run() {
            System.err.println("HOLA A TODOS");
            try {
                MyServerHandler.sendFightToAnotherClient(fs.getId_emisor(), fs.getId_receiver());
            } catch (SQLException e) {
            }
            //IoSession emisor = MyServerHandler.sockets.get(fs.getId_emisor());
            //IoSession receiver = MyServerHandler.sockets.get(fs.getId_receiver());
        }

    }

    static class SendFightThread extends Thread {

        protected long sessionPort;
        protected long receiver_id;
        protected String str;

        public SendFightThread(long s, long r, String msg) {
            this.sessionPort = s;
            this.receiver_id = r;
            this.str = msg;
        }

        @Override
        public void run() {
            MyServerHandler.sockets.values().parallelStream().forEach(ko -> {
                //System.out.println(sessionPort + " - " + ko.getId());
                long id = ko.getId();
                if (sessionPort != id) {
                    if (id == this.receiver_id) {
                        ko.write(getFight(sessionPort));
                    } else {
                        // Otherwise, we send a message to hide the fighters
                        ko.write(str);
                    }
                }
            }
            );
        }

    }

    static class SendAnothersThread extends Thread {

        protected long sessionPort;
        protected String str;

        public SendAnothersThread(long s, String msg) {
            this.sessionPort = s;
            this.str = msg;
        }

        @Override
        public void run() {
            MyServerHandler.sockets.values().forEach(ko -> {
                System.out.println(sessionPort + " - " + ko.getId() + " => " + str);
                if (sessionPort != ko.getId()) {
                    ko.write(str);
                }
            });
        }

    }

    @Override
    public void sessionClosed(IoSession session) {
        long port = session.getId();
        System.out.println(port);
        MyServerHandler.users_sockets.remove(port);
        MyServerHandler.sockets.remove(port);
        session.close();
        System.out.println("POSITIONS: " + MyServerHandler.users_sockets.size() + " - " + "SOCKETS: " + MyServerHandler.sockets.size());

        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = App.ds.getConnection();
            ps = conn.prepareStatement("UPDATE `users` SET `isAlive`=0 WHERE `port`=" + port + ";");
            ps.executeUpdate();
            new SendAnothersThread(session.getId(), getExitToClient(port)).start();
        } catch (SQLException ex) {
            System.out.println(ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    Logger.getLogger(MyServerHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(MyServerHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("Sesión creada ...");
    }

}
