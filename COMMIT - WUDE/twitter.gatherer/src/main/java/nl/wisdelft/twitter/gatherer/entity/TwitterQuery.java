/**
 * 
 */
package nl.wisdelft.twitter.gatherer.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * @author oosterman
 *
 */
@Entity
@Table(name="twitterquery")
public class TwitterQuery extends PersistentEntityGeneratedId {

		private Date dateExecuted;
		private String query;
		private Set<TwitterStatus> statusses = new HashSet<TwitterStatus>();
		private String errorMessage;
		
		public TwitterQuery(){
			
		}

		@Column(name="dateexecuted")
		public Date getDateExecuted() {
			return dateExecuted;
		}

		public void setDateExecuted(Date dateExecuted) {
			this.dateExecuted = dateExecuted;
		}

		@Column(name = "query")
		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		@ManyToMany(fetch=FetchType.LAZY,cascade=CascadeType.PERSIST)
		public Set<TwitterStatus> getStatusses() {
			return statusses;
		}

		public void setStatusses(Set<TwitterStatus> statusses) {
			this.statusses = statusses;
		}

		@Column(name="errormessage")
		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
		
		
}
