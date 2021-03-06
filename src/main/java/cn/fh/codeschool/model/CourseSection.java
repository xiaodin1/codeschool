package cn.fh.codeschool.model;
// Generated Apr 29, 2014 2:35:49 PM by Hibernate Tools 4.0.0

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * CourseSection generated by hbm2java
 */

@NamedQuery(
		name = "CourseSection.findCourseSections",
		query = "select c from CourseSection c where " +
				"c.courseChapter = :courseChapter"
		)

@Entity
@Table(name = "course_section")
public class CourseSection implements java.io.Serializable {

	private Integer id;
	private CourseChapter courseChapter;
	private String sectionDescription;
	private String courseContent;
	private Integer finishedMemberAmount;
	private String sectionName;
	
	private String initialCode; // 初始代码
	
	// 这个小节的所有post
	private List<Post> postList;
	
	
	/**
	 * 验证规则即小节通过的条件
	 * 一个小节可能会包含多个验证规则
	 */
	private List<ValidationRule> rules = new ArrayList<ValidationRule>();
	//private Set<ValidationRule> rules = new HashSet<ValidationRule>();

	public CourseSection() {
	}
	
	/**
	 * 完成人数 +1
	 */
	@Transient
	public void increaseFinishedAmount() {
		this.finishedMemberAmount++;
	}

	public CourseSection(int id, CourseChapter courseChapter) {
		this.id = id;
		this.courseChapter = courseChapter;
	}
	public CourseSection(Integer id, CourseChapter courseChapter,
			String courseContent, Integer finishedMemberAmount,
			String sectionName) {
		this.id = id;
		this.courseChapter = courseChapter;
		this.courseContent = courseContent;
		this.finishedMemberAmount = finishedMemberAmount;
		this.sectionName = sectionName;
	}

	@Id
	@GeneratedValue
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(name = "chapter_id", unique = false, nullable = false)
	@NotNull
	public CourseChapter getCourseChapter() {
		return this.courseChapter;
	}

	public void setCourseChapter(CourseChapter courseChapter) {
		this.courseChapter = courseChapter;
	}

	@Column(name = "course_content", columnDefinition = "TEXT")
	public String getCourseContent() {
		return this.courseContent;
	}

	public void setCourseContent(String courseContent) {
		this.courseContent = courseContent;
	}

	@Column(name = "finished_member_amount")
	public Integer getFinishedMemberAmount() {
		return this.finishedMemberAmount == null ? 0 : this.finishedMemberAmount;
	}

	public void setFinishedMemberAmount(Integer finishedMemberAmount) {
		this.finishedMemberAmount = finishedMemberAmount;
	}

	@Column(name = "section_name", length = 64)
	@Size(max = 64)
	public String getSectionName() {
		return this.sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinTable(name = "section_rule",
			joinColumns = @JoinColumn(name = "section_id"),
			inverseJoinColumns = @JoinColumn(name = "rule_id"))
	public List<ValidationRule> getRules() {
		return rules;
	}

	public void setRules(List<ValidationRule> rules) {
		this.rules = rules;
	}

	@Column(name = "initial_code")
	public String getInitialCode() {
		if (null != this.initialCode){
			//return initialCode.replace("\n", "\\n").replace("\t", "\\t");
		}
		System.out.println("code : " + this.initialCode);
		return this.initialCode;
	}

	public void setInitialCode(String initialCode) {
		this.initialCode = initialCode;
	}

	@OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
	public List<Post> getPostList() {
		return postList;
	}

	public void setPostList(List<Post> postList) {
		this.postList = postList;
	}

	@Column(name = "section_description")
	public String getSectionDescription() {
		return sectionDescription;
	}

	public void setSectionDescription(String sectionDescription) {
		this.sectionDescription = sectionDescription;
	}




}
