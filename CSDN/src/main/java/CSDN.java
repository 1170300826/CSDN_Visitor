import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class CSDN {
	public static Properties getProperties() {
		Properties properties = new Properties();
		InputStream resourceAsStream = CSDN.class.getResourceAsStream("config.properties");
		try {
			properties.load(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
	private String zhuye;
	private String sousuo = "/article/details/";
	public CSDN() {
		zhuye = getProperties().getProperty("The_home_page");
	}
	public CSDN(String url) {
		zhuye = url;
	}
	public String getZhuye() {
		return zhuye;
	}

	public void setZhuye(String zhuye) {
		this.zhuye = zhuye;
	}
	public String getSousuo() {
		return sousuo;
	}
	public void setSousuo(String sousuo) {
		this.sousuo = sousuo;
	}
	public String open(String url) {
		StringBuffer str = new StringBuffer();
		BufferedReader in = null;
		try {
			URL u = new URL(url);
			try {
				/*原来的代码*/
//				in = new BufferedReader(new InputStreamReader(u.openStream(), "UTF-8"));
				HttpURLConnection htpcon = (HttpURLConnection) u.openConnection();
				htpcon.setRequestMethod("GET");
				htpcon.setDoOutput(true);
				htpcon.setDoInput(true);
				htpcon.setUseCaches(false);
				//没有看到调用的地方，应该是在将之前的连接动态的排除异常
				htpcon.setConnectTimeout(10000);
				htpcon.setReadTimeout(10000);
				InputStream inputStream = htpcon.getInputStream();
				in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			} catch (SocketTimeoutException e) {
				System.out.println("本次请求超时了，别要慌张。");
				return "超时了";
			}
			while (true) {
				if (in != null) {
					String s = in.readLine();
					if (s == null) break;
					else str.append(s);
				}
			}
		} catch (SocketException e) {
			System.out.println("上面的这个代理失效了，请更换。");
		} catch (IOException e) {
			System.out.println("上面的这个代理失效了，请更换。");
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return str.toString();
	}

	public HashSet<String> sousuoHTML(String str) {
		HashSet<String> set = new HashSet<String>();
		int st, end;
		while ((st = str.indexOf(zhuye + sousuo)) != -1) {
			if ((end = str.indexOf("\"", st)) != -1) {
				String s = str.substring(st, end);
				if (s.indexOf("#comments") != -1) {
					s = s.substring(0, s.indexOf("#comments"));
				}
				set.add(s);
				str = str.substring(end);
			}
		}
		return set;
	}

	public int getFangke() {
		String str = open(zhuye);
		int i;
		if ((i = str.indexOf("访问：")) != -1) {
			str = str.substring(i);
			str = str.substring(str.indexOf("\"") + 1);
			str = str.substring(0, str.indexOf("\""));
		} else if ((i = str.indexOf("personal_list")) != -1) {
			str = str.substring(i);
			str.substring(str.indexOf("<em>") + 4, str.indexOf("</em>"));
		}
		int ii = 0;
		try {
			ii = Integer.parseInt(str);
		} catch (NumberFormatException e) {
		}
		return ii;
	}

	public void daili(String ip, String dk) {
		Properties prop = System.getProperties();
		// 设置http访问要使用的代理服务器的地址
		prop.setProperty("http.proxyHost", ip);
		// 设置http访问要使用的代理服务器的端口
		prop.setProperty("http.proxyPort", dk);
		// 设置不需要通过代理服务器访问的主机，可以使用*通配符，多个地址用|分隔
		prop.setProperty("http.nonProxyHosts", "localhost|192.168.168.*");
		// 设置安全访问使用的代理服务器地址与端口
		// 它没有https.nonProxyHosts属性，它按照http.nonProxyHosts 中设置的规则访问
		prop.setProperty("https.proxyHost", ip);
		prop.setProperty("https.proxyPort", dk);
		// 使用ftp代理服务器的主机、端口以及不需要使用ftp代理服务器的主机
		prop.setProperty("ftp.proxyHost", ip);
		prop.setProperty("ftp.proxyPort", dk);
		prop.setProperty("ftp.nonProxyHosts", "localhost|192.168.168.*");
		// socks代理服务器的地址与端口
		prop.setProperty("socksProxyHost", ip);
		prop.setProperty("socksProxyPort", dk);
		System.out.println("即将开始代理进行访问 ip:" + ip + " port:" + dk);
	}

	public static String[] dl = getProperties().getProperty("IP_And_Port").split(",");

	static class Main_thread implements Runnable {
		public void run() {
			AtomicInteger atomicInteger = new AtomicInteger();
			atomicInteger.set(1);
			int i = 0;
			CSDN csdn = new CSDN();
			while (true) {
				System.out.println("当前博客访问量：" + csdn.getFangke()+"");
				long a = System.currentTimeMillis();
				for (i = 0; i < dl.length; i++) {
					String[] dd = dl[i].split(":");
					csdn.daili(dd[0], dd[1]);
					HashSet<String> set = null;
					try {
						set = csdn.sousuoHTML(csdn.open(csdn.getZhuye()));
					} catch (Exception e) {
						System.out.println("上面的这个代理失效了，请更换。。。");
					}
					try {
						for (String url : set) {
							csdn.open(url);
							System.out.println("正在打开：" + url);
						}
					} catch (NullPointerException e) {
					}
				}
				System.out.println("---------------------------------------------------------------------------");
				System.out.println(" ");
				System.out.println("所有的代理已经访问：" + atomicInteger.getAndIncrement()+"次");
				if (csdn.getFangke() != 0) {
					System.out.println("当前博客访问量：" + csdn.getFangke()+"");
				}
				long b = System.currentTimeMillis();
				long c = b - a;
				System.out.println("本次代理请求耗时："+c+"秒");
			if(c>10000) {
				try {
					double v = Math.random() * 10;
					System.out.println("即将休息："+(long) (v*1000)+"毫秒");
					Thread.sleep((long) (v*1000));
					System.out.println("休息完成，即将开始下轮访问。");
					System.out.println(" ");
					System.out.println("---------------------------------------------------------------------------");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}
		}
	}

	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(1 );
		Main_thread main_thread = new Main_thread();
		for (int i = 0; i < 1; i++) {
			executorService.execute(main_thread);
		}
	}
}

