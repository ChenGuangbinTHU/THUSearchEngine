

import java.io.*;
import java.sql.*;
import java.util.*;


public class PageRank {

    private HashMap<Long, Item> nodeMap;
    private HashMap<Long, ArrayList<Long>> wikiGraph;
    private String tableName = "";

    private PageRank() {
        nodeMap = new HashMap<>();
        wikiGraph = new HashMap<>();
    }

    private void readNodeMap(String fileName) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
            String lineText;
            while ((lineText = bufferedReader.readLine()) != null) {
                String[] splitResult = lineText.split("-->");
                Long pageId = Long.valueOf(splitResult[1]);
                nodeMap.put(pageId, new Item(pageId, splitResult[0]));
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePR() {
        Connection conn;
        String mysqlUrl = "jdbc:mysql://localhost:3306/SchoolSearch";
        String mysqlUser = "";
        String mysqlPassword = "";
        try {
            conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
            for(Map.Entry<Long,Item> entry : nodeMap.entrySet()) {
                String sql = "UPDATE "+tableName+" SET "+tableName+".PR =" + entry.getValue().pageRank + "where "+tableName+".id=" +
                        entry.getKey();
                System.out.println(entry.getKey());
                PreparedStatement pstmt = conn.prepareStatement(sql);
                int rs = pstmt.executeUpdate();
            }
            conn.close();
//            PreparedStatement pstmt = conn.prepareStatement("UPDATE test SET test.id FROM test");
//            ResultSet rs = pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private void readWikiGraph() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Loaded MySQL Driver!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection conn;
        try {
            String mysqlUrl = "jdbc:mysql://localhost:3306/SchoolSearch";
            String mysqlUser = "root";
            String mysqlPassword = "password";
            conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
            PreparedStatement pstmt = conn.prepareStatement("SELECT id, url, out_links FROM "+tableName+"");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Select Done!");

            HashMap<String, Long> map = new HashMap<>();

            while (rs.next()) {
                Long pageId = rs.getLong(1);
                String url = rs.getString(2);
                map.put(url, pageId);
                nodeMap.put(pageId, new Item(pageId, url));
            }

            System.out.println("NodeMap.size = " + String.valueOf(nodeMap.size()));
            System.out.println("Map.size = " + String.valueOf(map.size()));

            rs.beforeFirst();

            while (rs.next()) {
                Long pageId = rs.getLong(1);
                String outLinkString = rs.getString(3);
                if (outLinkString == null) {
//                    ToDo: ???
                    continue;
                }
                ArrayList<Long> endPages = (wikiGraph.containsKey(pageId) ? wikiGraph.get(pageId) : new ArrayList<>());
                String[] outLinks = outLinkString.split("\n");

                for (String link : outLinks) {
                    if (link.contains("://")) {
                        link = link.split("://")[1];
                    }
                    if (map.containsKey(link)) {
                        endPages.add(map.get(link));
                    }
                }

                wikiGraph.put(pageId, endPages);
            }

            for (HashMap.Entry<Long, ArrayList<Long>> entry: wikiGraph.entrySet()) {
                if (nodeMap.containsKey(entry.getKey())) {
                    nodeMap.get(entry.getKey()).outDegree = entry.getValue().size();
                }
            }

            System.out.println(wikiGraph.size());

            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void readWikiGraph(String fileName) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
            String lineText;
            while ((lineText = bufferedReader.readLine()) != null) {
                String[] splitResult = lineText.split(":");
                Long pageId = Long.valueOf(splitResult[0]);
                if (splitResult.length > 1) {
                    ArrayList<Long> endPages = (wikiGraph.containsKey(pageId) ? wikiGraph.get(pageId) : new ArrayList<>());
                    String[] pages = splitResult[1].split(",");
                    for (String page : pages) {
                        endPages.add(Long.valueOf(page));
                    }
                    wikiGraph.put(pageId, endPages);
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculatePageRank() {
        int N = nodeMap.size();
        double initPageRank = (double) 1 / N, initI = 0.15 / N;

        for (Item item : nodeMap.values()) {
            item.pageRank = initPageRank;
            item.I = initI;
        }

        double S = 0.0;
        for (HashMap.Entry<Long, Item> entry : nodeMap.entrySet()) {
            if (!wikiGraph.containsKey(entry.getKey())) {
                S += entry.getValue().pageRank;
            }
        }

        System.out.println("S = " + S);
//        print('ALL PR = %.10f' % sum(map(lambda x: x[1]['PR'], node_map.items())))

        for (int k = 0; k < 30; ++k) {
            for (HashMap.Entry<Long, ArrayList<Long>> entry : wikiGraph.entrySet()) {
                Long i = entry.getKey();
                for (Long j : entry.getValue()) {
                    nodeMap.get(j).I += (1 - 0.15) * nodeMap.get(i).pageRank / entry.getValue().size();
                }
            }
            for (Item item : nodeMap.values()) {
                item.pageRank = item.I + (1 - 0.15) * S / N;
                item.I = initI;
            }

            S = 0.0;
            for (HashMap.Entry<Long, Item> entry : nodeMap.entrySet()) {
                if (!wikiGraph.containsKey(entry.getKey())) {
                    S += entry.getValue().pageRank;
                }
            }

            System.out.println("Round " + k + ": S = " + S);
        }

        System.out.println(nodeMap.size());
        System.out.println(wikiGraph.size());
    }

    private void printResult() {
        System.out.println("Begin sorting as +PR...");
        List<HashMap.Entry<Long, Item>> list = new ArrayList<>(nodeMap.entrySet());
        list.sort((o1, o2) -> {
            double pr1 = o1.getValue().pageRank, pr2 = o2.getValue().pageRank;
            return ((pr1 < pr2) ? 1 : ((pr1 == pr2) ? 0 : -1));
        });
        int count = 0;
        for (HashMap.Entry<Long, Item> entry: list) {
            System.out.println(entry.getKey() + ": " + entry.getValue().title + "   " + entry.getValue().pageRank + "   " + entry.getValue().outDegree);
            if (++count > 9) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        {
            PageRank pageRank = new PageRank();

            pageRank.readWikiGraph();

//            pageRank.readNodeMap("data/node.map.utf8");
//            pageRank.readWikiGraph("data/wiki.graph");

            pageRank.calculatePageRank();
            pageRank.printResult();
            pageRank.updatePR();

        }
        long endTime = System.currentTimeMillis();
        System.out.println((float) (endTime - startTime) / 1000);
    }

    private class Item {
        long pageId;
        String title;
        double pageRank, I;
        int inDegree, outDegree;

        Item(final long pageId, final String title) {
            this.pageId = pageId;
            this.title = title;
        }
    }
}