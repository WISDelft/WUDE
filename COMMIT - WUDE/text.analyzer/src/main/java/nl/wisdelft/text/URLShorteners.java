/**
 * 
 */
package nl.wisdelft.text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author oosterman
 */
public class URLShorteners {
	private static Set<String> shortener = new HashSet<String>();
	
	private URLShorteners(){}
	
	public static boolean isShortenedDomain(String domain){
		if(domain.startsWith("http")){
			return shortener.contains(domain);
		}
		else{
			return shortener.contains("http://"+domain) || shortener.contains("https://"+domain) ;
		}
	}
	
	public static String getUnshortenedUrl(String url) {
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setInstanceFollowRedirects(false);
			String loc = conn.getHeaderField("location");
			conn.disconnect();
			return loc;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		// something went wrong
		return null;
	}
	
	static {
		shortener.add("http://0rz.tw/");
		shortener.add("http://1link.in/");
		shortener.add("http://1url.com/");
		shortener.add("http://2.gp/");
		shortener.add("http://2big.at/");
		shortener.add("http://2tu.us/");
		shortener.add("http://3.ly/");
		shortener.add("http://301url.com/");
		shortener.add("http://307.to/");
		shortener.add("http://4ms.me/");
		shortener.add("http://4sq.com/");
		shortener.add("http://4url.cc/");
		shortener.add("http://6url.com/");
		shortener.add("http://7.ly/");
		shortener.add("http://a.gg/");
		shortener.add("http://a.nf/");
		shortener.add("http://a2n.eu/");
		shortener.add("http://aa.cx/");
		shortener.add("http://abcurl.net/");
		shortener.add("http://ad.vu/");
		shortener.add("http://adf.ly/");
		shortener.add("http://adjix.com/");
		shortener.add("http://afx.cc/");
		shortener.add("http://all.fuseurl.com/");
		shortener.add("http://alturl.com/");
		shortener.add("http://amzn.to/");
		shortener.add("http://ar.gy/");
		shortener.add("http://arst.ch/");
		shortener.add("http://atu.ca/");
		shortener.add("http://azc.cc/");
		shortener.add("http://b23.ru/");
		shortener.add("http://b2l.me/");
		shortener.add("http://b65.us/");
		shortener.add("http://bacn.me/");
		shortener.add("http://bcool.bz/");
		shortener.add("http://beam.to/");
		shortener.add("http://binged.it/");
		shortener.add("http://bit.ly/");
		shortener.add("http://bizj.us/");
		shortener.add("http://bloat.me/");
		shortener.add("http://bravo.ly/");
		shortener.add("http://bsa.ly/");
		shortener.add("http://budurl.com/");
		shortener.add("http://ByInter.com/");
		shortener.add("http://canurl.com/");
		shortener.add("http://chilp.it/");
		shortener.add("http://chzb.gr/");
		shortener.add("http://cl.lk/");
		shortener.add("http://cl.ly/");
		shortener.add("http://clck.ru/");
		shortener.add("http://cli.gs/");
		shortener.add("http://cliccami.info/");
		shortener.add("http://clickthru.ca/");
		shortener.add("http://clop.in/");
		shortener.add("http://conta.cc/");
		shortener.add("http://cort.as/");
		shortener.add("http://cot.ag/");
		shortener.add("http://crks.me/");
		shortener.add("http://ctvr.us/");
		shortener.add("http://cutt.us/");
		shortener.add("http://dai.ly/");
		shortener.add("http://decenturl.com/");
		shortener.add("http://dfl8.me/");
		shortener.add("http://digbig.com/");
		shortener.add("http://digg.com/");
		shortener.add("http://disq.us/");
		shortener.add("http://dld.bz/");
		shortener.add("http://dlvr.it/");
		shortener.add("http://do.my/");
		shortener.add("http://doiop.com/");
		shortener.add("http://dopen.us/");
		shortener.add("http://dwarfurl.com/");
		shortener.add("http://easyuri.com/");
		shortener.add("http://easyurl.net/");
		shortener.add("http://eepurl.com/");
		shortener.add("http://eweri.com/");
		shortener.add("http://fa.by/");
		shortener.add("http://fav.me/");
		shortener.add("http://fb.me/");
		shortener.add("http://fbshare.me/");
		shortener.add("http://ff.im/");
		shortener.add("http://fff.to/");
		shortener.add("http://fhurl.com/");
		shortener.add("http://fire.to/");
		shortener.add("http://firsturl.de/");
		shortener.add("http://firsturl.net/");
		shortener.add("http://flic.kr/");
		shortener.add("http://flq.us/");
		shortener.add("http://fly2.ws/");
		shortener.add("http://fon.gs/");
		shortener.add("http://freak.to/");
		shortener.add("http://fuseurl.com/");
		shortener.add("http://fuzzy.to/");
		shortener.add("http://fwd4.me/");
		shortener.add("http://fwdurl.net/");
		shortener.add("http://fwib.net/");
		shortener.add("http://g.ro.lt/");
		shortener.add("http://g8l.us/");
		shortener.add("http://gizmo.do/");
		shortener.add("http://gl.am/");
		shortener.add("http://go.9nl.com/");
		shortener.add("http://go.ign.com/");
		shortener.add("http://go.usa.gov/");
		shortener.add("http://goo.gl/");
		shortener.add("http://goshrink.com/");
		shortener.add("http://gurl.es/");
		shortener.add("http://hex.io/");
		shortener.add("http://hiderefer.com/");
		shortener.add("http://hmm.ph/");
		shortener.add("http://hongkiat.decenturl.com/");
		shortener.add("http://hongkiat.euro.st/");
		shortener.add("http://hongkiat.notlong.com");
		shortener.add("http://hongkiat.shorturl.com");
		shortener.add("http://href.in/");
		shortener.add("http://hsblinks.com/");
		shortener.add("http://htxt.it/");
		shortener.add("http://huff.to/");
		shortener.add("http://hulu.com/");
		shortener.add("http://hurl.me/");
		shortener.add("http://hurl.ws/");
		shortener.add("http://icanhaz.com/");
		shortener.add("http://idek.net/");
		shortener.add("http://ilix.in/");
		shortener.add("http://is.gd/");
		shortener.add("http://its.my/");
		shortener.add("http://ix.lt/");
		shortener.add("http://j.mp/");
		shortener.add("http://jijr.com/");
		shortener.add("http://kl.am/");
		shortener.add("http://klck.me/");
		shortener.add("http://korta.nu/");
		shortener.add("http://krunchd.com/");
		shortener.add("http://l9k.net/");
		shortener.add("http://lat.ms/");
		shortener.add("http://liip.to/");
		shortener.add("http://liltext.com/");
		shortener.add("http://linkbee.com/");
		shortener.add("http://linkbun.ch/");
		shortener.add("http://liurl.cn/");
		shortener.add("http://ln-s.net/");
		shortener.add("http://ln-s.ru/");
		shortener.add("http://lnk.gd/");
		shortener.add("http://lnk.in/");
		shortener.add("http://lnk.ms/");
		shortener.add("http://lnkd.in/");
		shortener.add("http://lnkurl.com/");
		shortener.add("http://lru.jp/");
		shortener.add("http://lt.tl/");
		shortener.add("http://lurl.no/");
		shortener.add("http://macte.ch/");
		shortener.add("http://mash.to/");
		shortener.add("http://merky.de/");
		shortener.add("http://migre.me/");
		shortener.add("http://minilien.com/");
		shortener.add("http://miniurl.com/");
		shortener.add("http://minurl.fr/");
		shortener.add("http://mke.me/");
		shortener.add("http://moby.to/");
		shortener.add("http://moourl.com/");
		shortener.add("http://moourl.com/woot/");
		shortener.add("http://mrte.ch/");
		shortener.add("http://myloc.me/");
		shortener.add("http://myurl.in/");
		shortener.add("http://n.pr/");
		shortener.add("http://nanoref.com/hongkiat/");
		shortener.add("http://nbc.co/");
		shortener.add("http://nblo.gs/");
		shortener.add("http://nn.nf/");
		shortener.add("http://not.my/");
		shortener.add("http://notlong.com/");
		shortener.add("http://nsfw.in/");
		shortener.add("http://nutshellurl.com/");
		shortener.add("http://nxy.in/");
		shortener.add("http://nyti.ms/");
		shortener.add("http://o-x.fr/");
		shortener.add("http://oc1.us/");
		shortener.add("http://om.ly/");
		shortener.add("http://omf.gd/");
		shortener.add("http://omoikane.net/");
		shortener.add("http://on.cnn.com/");
		shortener.add("http://on.mktw.net/");
		shortener.add("http://onforb.es/");
		shortener.add("http://orz.se/");
		shortener.add("http://ow.ly/");
		shortener.add("http://ping.fm/");
		shortener.add("http://piurl.com/");
		shortener.add("http://pli.gs/");
		shortener.add("http://pnt.me/");
		shortener.add("http://politi.co/");
		shortener.add("http://post.ly/");
		shortener.add("http://pp.gg/");
		shortener.add("http://profile.to/");
		shortener.add("http://ptiturl.com/");
		shortener.add("http://pub.vitrue.com/");
		shortener.add("http://qlnk.net/");
		shortener.add("http://qte.me/");
		shortener.add("http://qu.tc/");
		shortener.add("http://qy.fi/");
		shortener.add("http://r.im/");
		shortener.add("http://rb6.me/");
		shortener.add("http://read.bi/");
		shortener.add("http://readthis.ca/");
		shortener.add("http://reallytinyurl.com/");
		shortener.add("http://redir.ec/");
		shortener.add("http://redirects.ca/");
		shortener.add("http://redirx.com/");
		shortener.add("http://retwt.me/");
		shortener.add("http://ri.ms/");
		shortener.add("http://rickroll.it/");
		shortener.add("http://riz.gd/");
		shortener.add("http://rt.nu/");
		shortener.add("http://ru.ly/");
		shortener.add("http://rubyurl.com/");
		shortener.add("http://rurl.org/");
		shortener.add("http://rww.tw/");
		shortener.add("http://s4c.in/");
		shortener.add("http://s7y.us/");
		shortener.add("http://safe.mn/");
		shortener.add("http://sameurl.com/");
		shortener.add("http://sdut.us/");
		shortener.add("http://shar.es/");
		shortener.add("http://shink.de/");
		shortener.add("http://shorl.com/");
		shortener.add("http://short.ie/");
		shortener.add("http://short.to/");
		shortener.add("http://shorterlink.com/");
		shortener.add("http://shortlinks.co.uk/");
		shortener.add("http://shorturl.com/");
		shortener.add("http://shout.to/");
		shortener.add("http://show.my/");
		shortener.add("http://shredurl.com/");
		shortener.add("http://shrinkify.com/");
		shortener.add("http://shrinkr.com/");
		shortener.add("http://shrinkurl.us/");
		shortener.add("http://shrt.fr/");
		shortener.add("http://shrt.st/");
		shortener.add("http://shrten.com/");
		shortener.add("http://shrtnd.com/444/");
		shortener.add("http://shrunkin.com/");
		shortener.add("http://shurl.net/");
		shortener.add("http://simurl.com/");
		shortener.add("http://slate.me/");
		shortener.add("http://smallr.com/");
		shortener.add("http://smsh.me/");
		shortener.add("http://smurl.name/");
		shortener.add("http://sn.im/");
		shortener.add("http://snipr.com/");
		shortener.add("http://snipurl.com/");
		shortener.add("http://snurl.com/");
		shortener.add("http://sp2.ro/");
		shortener.add("http://spedr.com/");
		shortener.add("http://srnk.net/");
		shortener.add("http://srs.li/");
		shortener.add("http://starturl.com/");
		shortener.add("http://su.pr/");
		shortener.add("http://surl.co.uk/");
		shortener.add("http://surl.hu/");
		shortener.add("http://t.cn/");
		shortener.add("http://t.co/");
		shortener.add("http://t.lh.com/");
		shortener.add("http://ta.gd/");
		shortener.add("http://tbd.ly/");
		shortener.add("http://tcrn.ch/");
		shortener.add("http://tgr.me/");
		shortener.add("http://tgr.ph/");
		shortener.add("http://tighturl.com/");
		shortener.add("http://tiniuri.com/");
		shortener.add("http://tiny.cc/");
		shortener.add("http://tiny.ly/");
		shortener.add("http://tiny.pl/");
		shortener.add("http://tinylink.com/");
		shortener.add("http://tinylink.in/");
		shortener.add("http://tinyuri.ca/");
		shortener.add("http://tinyurl.com/");
		shortener.add("http://tk./");
		shortener.add("http://tl.gd/");
		shortener.add("http://tmi.me/");
		shortener.add("http://tnij.org/");
		shortener.add("http://tnw.to/");
		shortener.add("http://tny.com/");
		shortener.add("http://to./");
		shortener.add("http://to.ly/");
		shortener.add("http://togoto.us/");
		shortener.add("http://totc.us/");
		shortener.add("http://toysr.us/");
		shortener.add("http://tpm.ly/");
		shortener.add("http://tr.im/");
		shortener.add("http://tra.kz/");
		shortener.add("http://traceurl.com/");
		shortener.add("http://trunc.it/");
		shortener.add("http://twhub.com/");
		shortener.add("http://twirl.at/");
		shortener.add("http://twitclicks.com/");
		shortener.add("http://twitterurl.net/");
		shortener.add("http://twitterurl.org/");
		shortener.add("http://twiturl.de/");
		shortener.add("http://twurl.cc/");
		shortener.add("http://twurl.nl/");
		shortener.add("http://u.mavrev.com/");
		shortener.add("http://u.nu/");
		shortener.add("http://u76.org/");
		shortener.add("http://ub0.cc/");
		shortener.add("http://ulu.lu/");
		shortener.add("http://updating.me/");
		shortener.add("http://ur1.ca/");
		shortener.add("http://url.az/");
		shortener.add("http://url.co.uk/");
		shortener.add("http://url.ie/");
		shortener.add("http://url.lotpatrol.com/");
		shortener.add("http://url360.me/");
		shortener.add("http://url4.eu/");
		shortener.add("http://urlborg.com/");
		shortener.add("http://urlbrief.com/");
		shortener.add("http://urlcover.com/");
		shortener.add("http://urlcut.com/");
		shortener.add("http://urlenco.de/");
		shortener.add("http://urlhawk.com/");
		shortener.add("http://urli.nl/");
		shortener.add("http://urls.im/");
		shortener.add("http://urlshorteningservicefortwitter.com/");
		shortener.add("http://urltea.com/");
		shortener.add("http://urlvi.be/");
		shortener.add("http://urlx.ie/");
		shortener.add("http://urlzen.com/");
		shortener.add("http://usat.ly/");
		shortener.add("http://use.my/");
		shortener.add("http://vb.ly/");
		shortener.add("http://vgn.am/");
		shortener.add("http://vl.am/");
		shortener.add("http://vm.lc/");
		shortener.add("http://w55.de/");
		shortener.add("http://wapo.st/");
		shortener.add("http://wapurl.co.uk/");
		shortener.add("http://wipi.es/");
		shortener.add("http://wp.me/");
		shortener.add("http://www.6URL.com/");
		shortener.add("http://www.ezurl.eu/");
		shortener.add("http://www.liteurl.net/");
		shortener.add("http://www.shortenurl.com/");
		shortener.add("http://www.urlpire.com/");
		shortener.add("http://www.x.se/");
		shortener.add("http://x.vu/");
		shortener.add("http://xil.in/2323/");
		shortener.add("http://xr.com/");
		shortener.add("http://xrl.in/");
		shortener.add("http://xrl.us/");
		shortener.add("http://xurl.es/");
		shortener.add("http://xurl.jp/");
		shortener.add("http://y.ahoo.it/");
		shortener.add("http://yatuc.com/");
		shortener.add("http://ye.pe/");
		shortener.add("http://yep.it/");
		shortener.add("http://yfrog.com/");
		shortener.add("http://yhoo.it/");
		shortener.add("http://yiyd.com/");
		shortener.add("http://you.ne1.net");
		shortener.add("http://yourname.shim.net");
		shortener.add("http://youtu.be/");
		shortener.add("http://yuarel.com/");
		shortener.add("http://z0p.de/");
		shortener.add("http://zi.ma/");
		shortener.add("http://zi.mu/");
		shortener.add("http://zipmyurl.com/");
		shortener.add("http://zud.me/");
		shortener.add("http://zurl.ws/");
		shortener.add("http://zz.gd/");
		shortener.add("http://zzang.kr/");
	}
}
