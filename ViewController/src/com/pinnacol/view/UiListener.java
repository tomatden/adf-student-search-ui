package com.pinnacol.view;


import com.pinnacol.client.CourseClient;
import com.pinnacol.model.Course;

import com.pinnacol.model.StudentCourse;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.math.BigDecimal;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import javax.json.JsonObject;

import javax.transaction.Status;

import javax.ws.rs.core.MediaType;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.model.servlet.rest.payload.JSONParser;
import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;

import oracle.jbo.ViewObject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class UiListener {
    private RichInputText courseInputText;
    private RichTable studentsTable;

    public UiListener() {
    }
    
    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }
    
   private void queryStudents(String id){
        BigDecimal courseId = new BigDecimal (id);
        
        BindingContainer bindings = getBindings();
        OperationBinding operationBinding = bindings.getOperationBinding("ExecuteWithParams");
        operationBinding.getParamsMap().put("courseId", courseId); 
        
        Object result = operationBinding.execute();
        if (!operationBinding.getErrors().isEmpty()) {
            System.out.println("error executing with params");
        }         
       
    }
    

    public void CourseChangeListener(ValueChangeEvent valueChangeEvent) {
        
        String displayName = (String) courseInputText.getLocalValue();
        String id = null;
        List<Course> courseList = null; 
        
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        SessionData sessionData = (SessionData) ec.getSessionMap().get("sessionDataBean"); 
        
        if (sessionData == null){   //just in case, should not be null
            sessionData = new SessionData();
        try {
            courseList = CourseClient.getCourses();
            sessionData.setCourseList(courseList);
            ec.getSessionMap().put("sessionDataBean", sessionData);
        } catch (Exception e) {
            System.out.println("Error retrieving courses");
        }
        } else {  //should always get here, as sessionData populated in AutoSuggest
            courseList = sessionData.getCourseList(); 
        }
        
        for (Course course : courseList){
        if (displayName.contains(course.getCourseNumber())){
          id = course.getId().toString();  
          break;
        }
        }
        queryStudents(id);
    }

    public void setCourseInputText(RichInputText courseInputText) {
        this.courseInputText = courseInputText;
    }

    public RichInputText getCourseInputText() {
        return courseInputText;
    }

    public void removeBtnClick(ActionEvent actionEvent)  {
        System.out.println("Remove button click");
        
        DCBindingContainer bc = (DCBindingContainer) getBindings();
//        DCBindingContainer bc2 = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
        DCIteratorBinding rowIter1 = bc.findIteratorBinding("studentsForCourseIterator");
        
       
        Row r = rowIter1.getCurrentRow();
        String studentNameR = (String) r.getAttribute("name");
        System.out.println("row name " + studentNameR);

        RowSetIterator rowSIter1 = rowIter1.getRowSetIterator();
        Row r1 = rowSIter1.getCurrentRow();
        String studentName = (String) r1.getAttribute("name");
        String studentCourseId = ((BigDecimal) r1.getAttribute("id")).toString();
        int rowId = studentsTable.getRowIndex();
        System.out.println("delete name " + studentName + ", studentCourseId " + studentCourseId + " current row key is: " + rowId);
       
        String deleteUrl = "http://127.0.0.1:7101/adfRestServiceRmoug2015-service-context-root/resources/service/studentCourse/" + studentCourseId;
        Client client = Client.create();
        WebResource webResource = client.resource(deleteUrl);
     
        ClientResponse response = webResource.accept("application/json").delete(ClientResponse.class);

       if (response.getStatus() != 200) {
           FacesMessage msg = new FacesMessage("Delete Status", "Delete Failed : HTTP error code : " + response.getStatus());
           FacesContext.getCurrentInstance().addMessage("Delete status ", msg);
       }
        
        if (response.getStatus() == 200) {
           
           ViewObject vo = rowIter1.getViewObject();
           vo.removeCurrentRow();

           FacesMessage msg = new FacesMessage("Delete Status", studentName + " successfully un-enrolled");
           //FacesContext.getCurrentInstance().addMessage("Delete status ", msg);

        }

        
   }

    public void setStudentsTable(RichTable studentsTable) {
        this.studentsTable = studentsTable;
    }

    public RichTable getStudentsTable() {
        return studentsTable;
    }
}
