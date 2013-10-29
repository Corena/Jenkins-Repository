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
package com.nirima.jenkins.repo.build;

import com.nirima.jenkins.repo.RepositoryContent;
import hudson.maven.MavenBuild;
import hudson.maven.reporters.MavenArtifact;
import hudson.model.Run;

import com.nirima.jenkins.repo.MavenMetadata;
import com.nirima.jenkins.repo.RepositoryDirectory;
import com.nirima.jenkins.repo.RepositoryElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

/**
 * Represent a maven repository item.
 */
public class ArtifactRepositoryItem implements RepositoryContent {

    protected MavenArtifact artifact;
    private RepositoryDirectory directory;
    private MavenBuild build;
    private MavenMetadata metadata;

    public ArtifactRepositoryItem(MavenBuild build, MavenArtifact mavenArtifact)
    {
        this.artifact = mavenArtifact;
        this.build    = build;
        this.metadata = new MavenMetadata(mavenArtifact, build);
    }

    public String getName() {
        String fileName = metadata.getUniqueName();
        return fileName;
    }

    public RepositoryDirectory getParent() {
        return directory;
    }

    public void setParent(RepositoryDirectory parent)
    {
        this.directory = parent;
    }

    public String getPath() {
        return directory.getPath() + "/" + getName();
    }

    public InputStream getContent() throws Exception {
        return new FileInputStream(getFile());
    }

    public String getLastModified() {
        return "" + new Date( getFile().lastModified() ).toLocaleString();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Long getSize() {
        return getFile().length();
    }

    public String getDescription() {
        return "From Build #" + build.getNumber() + " of " + build.getParentBuild().getParent().getName();
    }

    public File getFile() {
        File fPath = new File(new File(new File(build.getArtifactsDir(), artifact.groupId), artifact.artifactId), artifact.version);
        File fArtifact;

        fArtifact = new File(fPath, artifact.canonicalName);
        if( fArtifact.exists() )
            return fArtifact;


        fArtifact = new File(fPath, artifact.fileName);
        if( fArtifact.exists() )
            return fArtifact;

        throw new IllegalStateException("Maven artifact cannot be found with name or canonicalName - " + artifact);
    }

    /**
     * The path that the artifact believes it belongs to.
     * @return
     */
    public String getArtifactPath()
    {
        return artifact.groupId.replace('.','/') + "/" + artifact.artifactId + '/' + artifact.version + "/" + artifact.canonicalName;
    }
    
    public String getBuildNo() {
        return Integer.toString(build.getNumber());
    }
    
    public MavenBuild getBuild() {
        return build;
    }
    
    public String getVersion() {
        return artifact.version;
    }
    
    public String getClassifier() {
        return artifact.classifier;
    }
    
    public String getType() {
        return artifact.type;
    }
    public String getGroupId() {
        return artifact.groupId;
    }
    public String getArtifactId() {
        return artifact.artifactId;
    }
    public String getMd5Sum() {
        return artifact.md5sum;
    }
    public MavenMetadata getMavenMetadata() {
        return metadata;
    }
}
