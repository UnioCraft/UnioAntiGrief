package com.uniocraft.antigrief;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLManager {
    private final ConnectionPoolManager pool;

    public SQLManager(Main plugin) {
        pool = new ConnectionPoolManager(plugin);
    }

    public int getUserID(String player) {
        String QUERY = "SELECT user_id FROM `website`.`xf_user` WHERE username = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                return res.getInt("user_id");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getPlayerIP(String player) {
        int userid = getUserID(player);

        String QUERY = "SELECT ip_id, user_id, content_type, content_id, action, log_date, INET_NTOA(CONV(HEX(ip), 16, 10)) AS ip FROM `website`.`xf_ip` WHERE user_id = " + userid + " ORDER BY ip_id DESC LIMIT 1";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                return res.getString("ip");
            } else {
                return "0";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public boolean checkPlayerIP(String player, String IP) {
        String ip = getPlayerIP(player);
        return IP.equalsIgnoreCase(ip);
    }

    public void onDisable() {
        pool.closePool();
    }

}