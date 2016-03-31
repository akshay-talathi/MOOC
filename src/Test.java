import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.netty.hibernate.HibernateUtil;
import com.netty.pojo.NodeLog;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
			Session session = sessionFactory.openSession();
			session.beginTransaction();
			NodeLog log= new  NodeLog();
			log.setCommitIndex(1);
			log.setTermId(9);
			session.save(log);
			session.getTransaction().commit();
			session.close();
			sessionFactory.close();		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}