import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.jvnet.substance.SubstanceLookAndFeel;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;
import java.io.File;
import java.io.FileOutputStream;

import java.util.Properties;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

class JsonConverter {

	public static void showTrends(String fileName) {
		try {
			JSONParser parser = new JSONParser();
			FileInputStream fileInputStream = new FileInputStream(new File(
					fileName));
			byte[] byt = new byte[fileInputStream.available()];
			fileInputStream.read(byt);
			String s = new String(byt);
			try {
				Object obj = parser.parse(s);
				JSONArray jsonArray = (JSONArray) obj;
				JSONObject jsonObject = (JSONObject) jsonArray.get(0);
				JSONArray jArray = (JSONArray) jsonObject.get("trends");
				for( int i = 0; i < jArray.size(); i++) {
					JSONObject jObject = (JSONObject) jArray.get(i);
					MainForm.jComboTrend.addItem(jObject.get("name").toString());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			fileInputStream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showUsers(String fileName) {
		try {
			JSONParser parser = new JSONParser();
			FileInputStream fileInputStream = new FileInputStream(new File(
					fileName));
			byte[] byt = new byte[fileInputStream.available()];
			fileInputStream.read(byt);
			String s = new String(byt);
			MainForm.defaultTableUser.fireTableDataChanged();
			try {
				Object obj = parser.parse(s);
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray trends = (JSONArray) jsonObject.get("statuses");
				System.out.println("No. of Users: "+trends.size());

				int c = 0;
				for (int i = 0; i < trends.size(); i++) {
					int flag = 0;
					JSONObject jsObject = (JSONObject) trends.get(i);
					JSONObject jObject = (JSONObject) jsObject.get("user");
					Vector<String> rowData = new Vector<String>();
					for ( int j = 0; j < MainForm.defaultTableUser.getRowCount(); j++) {
						String getID = MainForm.defaultTableUser.getValueAt(j,1).toString();
						String addID = jObject.get("id").toString();
						if(getID.equals(addID)) {
							flag = 1;
						}
						}

					if(flag == 0) {
						c=c+1;
					String sno = Integer.toString(c);
					rowData.add(sno);
					rowData.add(jObject.get("id").toString());
					rowData.add(jObject.get("name").toString());
					rowData.add(jObject.get("screen_name").toString());
					rowData.add(jsObject.get("id").toString());
					MainForm.defaultTableUser.addRow(rowData);
					MainForm.jComboUserNo.addItem(sno);
					}
					}

				System.out.println("RowCount:" + MainForm.defaultTableUser.getRowCount());

			}
			catch (Exception e) {
				e.printStackTrace();
			}
			fileInputStream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showTweets(String fileNo) {
		try {
			String fileName = "Tweets\\"+fileNo+".txt";

			JSONParser parser = new JSONParser();
			FileInputStream fileInputStream = new FileInputStream(new File(
					fileName));
			byte[] byt = new byte[fileInputStream.available()];
			fileInputStream.read(byt);
			String s = new String(byt);

			try {
				Object obj = parser.parse(s);
				JSONArray jsonArray = (JSONArray) obj;
				for(int i = 0; i< jsonArray.size(); i++){
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					Vector<String> rowData = new Vector<String>();
					JSONObject entities = (JSONObject) jsonObject.get("entities");
					JSONArray hashtags = (JSONArray) entities.get("hashtags");
					String firstColumn = null;
					JSONArray userMentions = (JSONArray) entities.get("user_mentions");
					if(hashtags.size() > 0) {
						for(int j = 0; j < hashtags.size(); j++) {
							JSONObject hashtagObj = (JSONObject) hashtags.get(j);
							String data1 = hashtagObj.get("text").toString();
							if(j == 0) {
								firstColumn = data1;
							}
							else {
								firstColumn += data1;
							}
							if(j < hashtags.size() - 1) {
								firstColumn += ", ";
							}
						}
					}
					else {
						firstColumn = "None";
					}
					String secondColumn = null;
					if(userMentions.size() > 0) {
						for(int j = 0; j < userMentions.size(); j++) {
							JSONObject user_id = (JSONObject) userMentions.get(j);
							String data2 = user_id.get("id").toString();
							//user.add(data2);
							if(j == 0) {
								secondColumn =  data2;
							}
							else {
								secondColumn +=  data2;
							}
							if(j < userMentions.size() - 1) {
								secondColumn += ", ";
							}
						}
					}
					else {
						secondColumn = "None";
					}
					rowData.add(firstColumn);
					rowData.add(secondColumn);
					MainForm.defaultTableTweets.addRow(rowData);
				}
				System.out.println(MainForm.defaultTableTweets.getRowCount());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			fileInputStream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Training{
	static Vector<Long> idVector = new Vector<Long>();//All the user mentions
	public static Long[][] userMentions;//This has count
	static int size = 0;//No. of elements in userMentions[][]
	static String resid;
	static double UpperW;
	static int random1=0;

	public static void findOutliers() {
		try {
			for(int a = 1; a <= MainForm.defaultTableUser.getRowCount(); a++) {
				String fileNo = Integer.toString(a);
				String fileName = "Tweets\\"+fileNo+".txt";

				JSONParser parser = new JSONParser();
				FileInputStream fileInputStream = new FileInputStream(new File(
						fileName));
				byte[] byt = new byte[fileInputStream.available()];
				fileInputStream.read(byt);
				String s = new String(byt);

				try {
					Object obj = parser.parse(s);
					JSONArray jsonArray = (JSONArray) obj;
					for(int i = 0; i < jsonArray.size(); i++){
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						JSONObject entities = (JSONObject) jsonObject.get("entities");
						JSONArray userMentions = (JSONArray) entities.get("user_mentions");

						if(userMentions.size() > 0) {
							for(int j = 0; j < userMentions.size(); j++) {
								JSONObject user_id = (JSONObject) userMentions.get(j);
								Long data = Long.parseLong(user_id.get("id").toString());
								idVector.add(data);
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				fileInputStream.close();
			}
			System.out.println("Size: "+idVector.size());
			countData();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void countData() {
		userMentions = new Long[idVector.size()][2];
		long count;
		for(int i = 0; i < idVector.size(); i++) {
			size += 1;
			userMentions[i][0] = idVector.get(i);
			long ifVar = idVector.get(i);
			count = 1;
			for(int j = i + 1; j < idVector.size(); j++) {
				if(ifVar == idVector.get(j)) {
					count += 1;
					idVector.remove(j);
				}
			}
			userMentions[i][1] = count;

		}
		System.out.println("New Size: "+size);
		sortData();
	}

	public static void sortData() {
		for(int i = 0; i < size; i++) {
			for(int j = i + 1; j < size; j++) {
				if(userMentions[i][1] > userMentions[j][1]) {
					long temp1 = userMentions[i][0];
					long temp2 = userMentions[i][1];
					userMentions[i][0] = userMentions[j][0];
					userMentions[i][1] = userMentions[j][1];
					userMentions[j][0] = temp1;
					userMentions[j][1] = temp2;
				}
			}
		}
		calculation();
	}


	public static void calculation() {
		long Q1,Q3,IQR;
		int size2;
		if(size % 2 == 0) {
			size2 = size / 2;
				if(size2 % 2 == 0) {
					Q1 = (userMentions[(size2/2)-1][1] + userMentions[(size2/2)][1])/2;
					Q3 = (userMentions[(size/2) + (size2/2) - 1][1] + userMentions[(size/2) + (size2/2)][1])/2;
				}
				else {
					Q1 = userMentions[(size2/2)][1];
					Q3 = userMentions[(size/2 + size2/2)][1];
				}
		}
		else {
			size2 = size / 2;
			if(size2 % 2 == 0) {
				Q1 = (userMentions[(size2/2)-1][1]+userMentions[(size2/2)][1])/2;
				Q3 = (userMentions[(size/2)+(size2/2)][1] + userMentions[(size/2)+(size2/2) + 1][1])/2;
			}
			else {
				Q1 = userMentions[size2/2][1];
				Q3 = userMentions[(size2/2) + (size/2) + 1][1];
			}
		}
		System.out.println(Q1+ " " + Q3);
		IQR = Q3 - Q1;
		System.out.println(IQR);
		UpperW = Q3 + (1.5 * IQR);
		System.out.println(UpperW);

	}

	public static void getResults() {
		String[][] hashtagCount = new String[size][2];
		int hashIndex = 0;
		String[][] idCount = new String[size][2];
		int idIndex = 0;
		int flag,flag2;
		Vector<String> fin = new Vector<String>();
		try {
			JSONParser parser = new JSONParser();
			FileInputStream fileInputStream = new FileInputStream(new File("users.txt"));
			byte[] byt = new byte[fileInputStream.available()];
			fileInputStream.read(byt);
			String s = new String(byt);

			try {
				Object obj = parser.parse(s);
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray trends = (JSONArray) jsonObject.get("statuses");
				for (int i = 0; i < trends.size(); i++) {
					JSONObject jsObject = (JSONObject) trends.get(i);
					JSONObject jObject = (JSONObject) jsObject.get("entities");
					JSONArray hashtags = (JSONArray) jObject.get("hashtags");
					JSONArray user_mentions = (JSONArray) jObject.get("user_mentions");
					for(int a = 0; a < hashtags.size(); a++) {
						flag = 0;
						JSONObject hashObj = (JSONObject) hashtags.get(a);
						String hashText = hashObj.get("text").toString();
						for(int x = 0; x < hashIndex; x++) {
							if(hashText.equals(hashtagCount[x][0])) {
								int num = Integer.parseInt(hashtagCount[x][1]);
								num += 1;
								hashtagCount[x][1] = Integer.toString(num);
								flag = 1;
								break;
							}
						}
						if(flag == 0) {
							hashtagCount[hashIndex][0] = hashText;
							hashtagCount[hashIndex][1] = "1";
							hashIndex += 1;
						}
					}
					for(int b = 0; b < user_mentions.size(); b++) {
						JSONObject userID = (JSONObject) user_mentions.get(b);
						String Id = userID.get("id").toString();
						flag2 = 0;
						for(int y = 0; y < idIndex; y++) {
							if(Id.equals(idCount[y][0])) {
								int num = Integer.parseInt(idCount[y][1]);
								num += 1;
								idCount[y][1] = Integer.toString(num);
								flag2 = 1;
								break;
							}
						}
						if(flag2 == 0) {
							idCount[idIndex][0] = Id;
							idCount[idIndex][1] = "1";
							idIndex += 1;
						}
					}
					for(int a = 0; a < idIndex; a++) {
						if(UpperW < Integer.parseInt(idCount[a][1])) {
							resid = idCount[a][0];
							System.out.println("Id: "+resid);
							random1 = 1;
							break;
						}
					}
					if(random1==1) {
						for(int a = 0; a < hashtags.size(); a++) {
						flag = 0;
						JSONObject hashObj = (JSONObject) hashtags.get(a);
						String hashText = hashObj.get("text").toString();
						fin.add(hashText);
						}
					}
					if(random1 == 1) {
						break;
					}
				}
				Vector<String> output = new Vector<String>();
				int num2 = 0;
				for(int i = 0; i < fin.size(); i++) {
					for(int j = 0; j < hashIndex; j++){
						if(fin.get(i).equals(hashtagCount[j][0])) {
							int temp = Integer.parseInt(hashtagCount[j][1]);
							if(temp > num2) {
								output.clear();
								num2 = temp;
								output.add(hashtagCount[j][0]);
								output.add(hashtagCount[j][1]);
							}
						}
					}
				}
				MainForm.defaultTableResult.addRow(output);

			}
			catch(Exception e) {
				e.printStackTrace();
			}
			fileInputStream.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}


class TwitterRestCall {
	static String consumerKeyStr = "";
	static String consumerSecretStr = "";
	static String accessTokenStr = "";
	static String accessTokenSecretStr = "";
	static OAuthConsumer consumer;
	public TwitterRestCall() {
		consumerKeyStr= readProperties("consumerKey");
		consumerSecretStr=readProperties("consumerSecret");
		accessTokenStr=readProperties("accessToken");
		accessTokenSecretStr=readProperties("accessTokenSecret");
		consumer = new CommonsHttpOAuthConsumer(consumerKeyStr,consumerSecretStr);
		consumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
	}

	private static String readProperties(String key) {
		String url = "";
		try {
			Properties properties = new Properties();
			FileInputStream fileInputStream = new FileInputStream(new File(
					"configuration.properties"));
			properties.load(fileInputStream);
			url = properties.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	public void getTrends() {
		try {
			String url = readProperties("getTrends");

			DefaultHttpClient client = new DefaultHttpClient();
			client.getCredentialsProvider().setCredentials(
				    new AuthScope("192.168.23.32", 3128),
				    new UsernamePasswordCredentials("1212115109", "Revelation"));
			HttpHost host = new HttpHost("192.168.23.32",3128);
			client.getParams()
			.setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
			HttpGet request = new HttpGet(url);
			consumer.sign(request);

			HttpResponse response = client.execute(request);

			FileOutputStream fileOutputStream = new FileOutputStream(new File(
					"trends.txt"));
			fileOutputStream.write(IOUtils.toString(
					response.getEntity().getContent()).getBytes());
			fileOutputStream.close();
			JsonConverter.showTrends("trends.txt");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getUsers(String trendName) {
		try {

			String url = readProperties("search");
			trendName = trendName.replace("#", "%23");
			trendName = trendName.replaceAll(" ", "=");

			url += "="+trendName+"&count=30"+"&tweet_mode=extended";

			DefaultHttpClient client = new DefaultHttpClient();
			client.getCredentialsProvider().setCredentials(
				    new AuthScope("192.168.23.32", 3128),
				    new UsernamePasswordCredentials("1212115109", "Revelation"));
			HttpHost host = new HttpHost("192.168.23.32",3128);
			client.getParams()
			.setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
			HttpGet request = new HttpGet(url);
			consumer.sign(request);
			HttpResponse response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			System.out.println(statusCode + ":"
					+ response.getStatusLine().getReasonPhrase());
			FileOutputStream fileOutputStream = new FileOutputStream(new File(
					"users.txt"));

			fileOutputStream.write(IOUtils.toString(
					response.getEntity().getContent()).getBytes());
			fileOutputStream.close();
			JsonConverter.showUsers("users.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getData() {

		int count = MainForm.defaultTableUser.getRowCount();

		for(int i = 0; i < count; i++) {
			String url = readProperties("getTweets");
			String user_id = MainForm.defaultTableUser.getValueAt(i,1).toString();
			String max_id = MainForm.defaultTableUser.getValueAt(i,4).toString();
			long maxId = Long.parseLong(max_id) - 1;
			max_id = Long.toString(maxId);
			url += "id="+user_id+"&count=40"+"&max_id="+maxId;
			try {
				DefaultHttpClient client = new DefaultHttpClient();
				client.getCredentialsProvider().setCredentials(
					    new AuthScope("192.168.23.32", 3128),
					    new UsernamePasswordCredentials("1212115109", "Revelation"));
				HttpHost host = new HttpHost("192.168.23.32",3128);
				client.getParams()
				.setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
				HttpGet request = new HttpGet(url);
				consumer.sign(request);
				HttpResponse response = client.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				System.out.println(statusCode + ":"
						+ response.getStatusLine().getReasonPhrase());

				String filenum = Integer.toString(i + 1);
				String fileName = "Tweets\\"+filenum+".txt";
				FileOutputStream fileOutputStream = new FileOutputStream(new File(
						fileName));

				fileOutputStream.write(IOUtils.toString(
						response.getEntity().getContent()).getBytes());
				fileOutputStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}

public class MainForm implements ActionListener {
	private JButton jButtonTrends, jButtonUsers, jButtonData, jButtonTweets, jButtonTraining, jButtonResult;
	public static DefaultTableModel defaultTableUser, defaultTableTweets, defaultTableResult;
	private JTable jTableUser, jTableTweets, jTableResult;
	private JScrollPane jScrollPaneUser, jScrollPaneTweets, jScrollPaneResult;
	public static JComboBox<String> jComboTrend;
	public static JComboBox<String> jComboUserNo;

	private TwitterRestCall twitterRestCall;

	static {
		try {
			SubstanceLookAndFeel
					.setCurrentWatermark("org.jvnet.substance.watermark.SubstanceBinaryWatermark");
			SubstanceLookAndFeel
					.setCurrentTheme("org.jvnet.substance.theme.SubstanceInvertedTheme");
			SubstanceLookAndFeel
					.setCurrentGradientPainter("org.jvnet.substance.painter.SpecularGradientPainter");
			SubstanceLookAndFeel
					.setCurrentButtonShaper("org.jvnet.substance.button.ClassicButtonShaper");
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void design() {

		twitterRestCall = new TwitterRestCall();
		JFrame frame = new JFrame("Discovering Emerging Topics");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTabbedPane tab = new JTabbedPane();
		frame.add(tab, BorderLayout.CENTER);
		frame.add(createPanelLayout());
		frame.setSize(1600, 770);
		frame.setVisible(true);
	}

	public JPanel createPanelLayout() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(null);

		jComboTrend = new JComboBox<String>();//Shows the trends
		jComboTrend.setBounds(10, 30, 150, 30);
		jPanel.add(jComboTrend);

		jButtonTrends = new JButton("Get Trends");
		jButtonTrends.addActionListener(this);
		jButtonTrends.setBounds(180,30,150,30);
		jPanel.add(jButtonTrends);

		jButtonUsers = new JButton("Get Users");//Get tweets relevant to trend
		jButtonUsers.addActionListener(this);
		jButtonUsers.setBounds(10, 80, 150, 30);
		jPanel.add(jButtonUsers);

		jButtonData = new JButton("Get Tweets");//Get tweets of the previous found users
		jButtonData.setBounds(10, 160, 100, 30);
		jButtonData.addActionListener(this);
		jPanel.add(jButtonData);

		jButtonTraining = new JButton("Get Min Outlier Value");//Start Training
		jButtonTraining.setBounds(120, 160, 140, 30);
		jButtonTraining.addActionListener(this);
		jPanel.add(jButtonTraining);

		jButtonResult = new JButton("Show Result");//Show Result
		jButtonResult.setBounds(270, 160, 140, 30);
		jButtonResult.addActionListener(this);
		jPanel.add(jButtonResult);

		defaultTableUser = new DefaultTableModel();//Shows details of targeted users
		defaultTableUser.addColumn("S.No.");
		defaultTableUser.addColumn("ID");
		defaultTableUser.addColumn("Username");
		defaultTableUser.addColumn("Screen Name");
		defaultTableUser.addColumn("Tweet Id");
		jTableUser = new JTable(defaultTableUser);
		jTableUser.getColumnModel().getColumn(0).setPreferredWidth(80);
		jTableUser.getColumnModel().getColumn(1).setPreferredWidth(160);
		jTableUser.getColumnModel().getColumn(2).setPreferredWidth(160);
		jTableUser.getColumnModel().getColumn(3).setPreferredWidth(160);
		jTableUser.getColumnModel().getColumn(4).setPreferredWidth(160);
		jPanel.add(jTableUser);

		jScrollPaneUser = new JScrollPane(jTableUser);
		jScrollPaneUser.setBounds(450, 30, 720, 200);
		jPanel.add(jScrollPaneUser);

		jComboUserNo = new JComboBox<String>();//Shows User s. no
		jComboUserNo.setBounds(10,380,150,30);
		jPanel.add(jComboUserNo);

		jButtonTweets = new JButton("Show Tweets");//Shows Tweets of selected User
		jButtonTweets.setBounds(200,380,160,30);
		jButtonTweets.addActionListener(this);
		jPanel.add(jButtonTweets);

		defaultTableTweets = new DefaultTableModel();//Shows The tweets components
		defaultTableTweets.addColumn("Hashtag");
		defaultTableTweets.addColumn("User Mentions");
		jTableTweets = new JTable(defaultTableTweets);
		jPanel.add(jTableTweets);

		jScrollPaneTweets = new JScrollPane(jTableTweets);
		jScrollPaneTweets.setBounds(10, 470, 550, 200);
		jPanel.add(jScrollPaneTweets);

		defaultTableResult = new DefaultTableModel();
		defaultTableResult.addColumn("Trends");
		defaultTableResult.addColumn("Count");
		jTableResult = new JTable(defaultTableResult);
		jPanel.add(jTableResult);

		jScrollPaneResult = new JScrollPane(jTableResult);
		jScrollPaneResult.setBounds(650,470,300,200);
		jPanel.add(jScrollPaneResult);
		return jPanel;
	}

	public static void main(String[] args) {
		MainForm mainform = new MainForm();
		mainform.design();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jButtonTrends){
			if(defaultTableUser.getRowCount() > 0 || defaultTableTweets.getRowCount() > 0) {
				removeData(defaultTableUser);
				removeData(defaultTableTweets);
			}
			twitterRestCall.getTrends();
		}
		else if (e.getSource() == jButtonUsers) {
			if(defaultTableUser.getRowCount() > 0) {
				removeData(defaultTableUser);
			}
			 String trendName = jComboTrend.getSelectedItem().toString();
			twitterRestCall.getUsers(trendName);
		}
		else if (e.getSource() == jButtonData) {
			if(defaultTableUser.getRowCount() <= 0) {
				System.out.println("Error");
			}
			else {
				twitterRestCall.getData();
			}
		}
		else if (e.getSource() == jButtonTweets) {
			if(defaultTableTweets.getRowCount() > 0) {
				removeData(defaultTableTweets);
			}
			String fileNo = jComboUserNo.getSelectedItem().toString();
			JsonConverter.showTweets(fileNo);
		}
		else if (e.getSource() == jButtonTraining) {
			Training.findOutliers();
		}
		else if (e.getSource() == jButtonResult) {
			Training.getResults();
		}
	}

	public void removeData(DefaultTableModel defaultTable) {
		for(int i = defaultTable.getRowCount() - 1; i > -1; i--) {
			defaultTable.removeRow(i);
		}
	}


}
