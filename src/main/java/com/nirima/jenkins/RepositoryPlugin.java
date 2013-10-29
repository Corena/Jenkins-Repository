/*
 * The MIT License
 *
 * Copyright (c) 2011, Nigel Magnay / NiRiMa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.nirima.jenkins;

import com.nirima.jenkins.repo.RootElement;
import hudson.Extension;
import hudson.Plugin;
import hudson.model.*;
import hudson.util.IOUtils;
import com.nirima.jenkins.repo.RepositoryContent;
import com.nirima.jenkins.repo.project.ProjectsElement;
import com.nirima.jenkins.repo.RepositoryDirectory;
import com.nirima.jenkins.repo.RepositoryElement;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import hudson.plugins.git.util.BuildData;


@Extension
public class RepositoryPlugin extends Plugin {
    private ServletContext context;
    private static final Logger LOGGER = Logger.getLogger(RepositoryPlugin.class.getName());

    public RepositoryPlugin() {

    }

    @Override
    public void start() {

    }

    public void setServletContext(ServletContext context) {
        this.context = context;
    }

    @Override
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();

        if (path.length() == 0)
            path = "/";

        if (path.indexOf("..") != -1 || path.length() < 1) {
            // don't serve anything other than files in the sub directory.
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        RepositoryElement currentItem = new RootElement();

        // Split into sections
        String[] pathElements = path.substring(1).split("/");

        try {
            // Ignore breakdown case if '/'
            if (pathElements.length > 1 || pathElements[0].length() > 0) {
                for (String element : pathElements) {
                    if (currentItem instanceof RepositoryDirectory) {
                        RepositoryDirectory currentDirectory = (RepositoryDirectory) currentItem;
                        currentItem = currentDirectory.getChild(element);                        
                    } 

                }
            }

            displayElement(req, rsp, currentItem);

        } catch (Exception ex) {
            // try static content
            super.doDynamic(req, rsp);
        }
    }

    private void displayElement(StaplerRequest req, StaplerResponse rsp, RepositoryElement currentItem) throws Exception {
        OutputStream os = rsp.getOutputStream();
        try {
            if (currentItem instanceof RepositoryDirectory) {
    
                rsp.setContentType("text/html;charset=UTF-8");
    
                printHeader(os, req, (RepositoryDirectory) currentItem);
    
                for (RepositoryElement element : ((RepositoryDirectory) currentItem).getChildren()) {
                    printDirEntry(os, element);
                }
    
                printFooter(os);
            } else if (currentItem != null) {
                RepositoryContent content = (RepositoryContent) currentItem;
                if (content.getName().endsWith(".xml")) 
                    rsp.setContentType("text/xml;charset=UTF-8");
                InputStream is = content.getContent();
                // DL Element
                IOUtils.copy(is, os);
    
                os.flush();
    
            } else {
            	rsp.sendError(StaplerResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            rsp.sendError(StaplerResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    private Build getBuild(Project theProject, String type, String ref) {
        if (type.equals("build")) {
            int nbr = Integer.parseInt(ref);

            for (Object object : theProject.getBuilds()) {
                Build r = (Build) object;
                if (r.getNumber() == nbr)
                    return r;
            }
        } else {
            for (Object object : theProject.getBuilds()) {
                Build r = (Build) object;
                BuildData bd = r.getAction(BuildData.class);
                if (bd != null && bd.getLastBuiltRevision().getSha1String().equals(ref))
                    return r;
            }
        }

        return null;
    }


    private void printHeader(OutputStream os,StaplerRequest req, RepositoryDirectory directory) throws IOException {
        String title = "<html>\n" +
                "  <head>\n" +
                "    <title>Index of " + directory.getPath() + "</title>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n" +
                "    <link rel=\"stylesheet\" href=\"" + req.getContextPath() + "/plugin/repository/css/repository-style.css\" type=\"text/css\" media=\"screen\" title=\"no title\" charset=\"utf-8\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Index of " + directory.getPath() + "</h1>\n" +
                "    <table cellspacing=\"10\">\n" +
                "      <tr>\n" +
                "        <th align=\"left\">Name</th>\n" +
                "        <th>Last Modified</th>\n" +
                "        <th>Size</th>\n" +
                "        <th>Description</th>\n" +
                "      </tr>";

        String parent = "<tr>\n" +
                "        <td>\n" +
                "          <a href=\"../\">Parent Directory</a>\n" +
                "        </td>\n" +
                "      </tr>";

        os.write(title.getBytes("UTF-8"));

        if (directory.getParent() != null) {
            os.write(parent.getBytes("UTF-8"));
        }


    }

    private void printFooter(OutputStream os) throws IOException {
        String footer =
                "            </table>\n" +
                        "  </body>\n" +
                        "</html>";

        os.write(footer.getBytes("UTF-8"));
    }

    private void printDirEntry(OutputStream os, RepositoryElement item) throws IOException {

        String name = item.getName();
        String lastModified = "";
        String size = "";
        String description = "";

        if (item instanceof RepositoryDirectory)
            name += "/";
        if ( item instanceof RepositoryContent)
        {
            lastModified = ((RepositoryContent)item).getLastModified();
            size =  "" + ((RepositoryContent)item).getSize();
            description =   ((RepositoryContent)item).getDescription();
        }

         String entry = "      <tr>\n" +
                "            <td>\n" +
                "                              <a href=\"" + name + "\">" + name + "</a>\n" +
                "                          </td>\n" +
                "            <td>\n" +
                "              " + lastModified + "\n" +
                "            </td>\n" +
                "            <td align=\"right\">\n" +
                "                   " + size + "\n" +
                "                          </td>\n" +
                "            <td>\n" +
                "              " + description + "\n" +
                "            </td>\n" +
                "          </tr>";

        os.write(entry.getBytes("UTF-8"));

    }


    private Project getProject(String pathElement) {
        for (Project project : Hudson.getInstance().getProjects()) {
            if (project.getName().equals(pathElement))
                return project;
        }
        return null;
    }


    public static String DISPLAY_NAME = "Jenkins Maven Repository Server";
    public static String URL = "repository";
}
