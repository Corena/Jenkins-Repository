package com.nirima.jenkins.repo;

import hudson.maven.MavenBuild;
import hudson.maven.artifact.transform.SnapshotTransformation;
import hudson.maven.reporters.MavenArtifact;

import java.util.Date;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.StringUtils;

public class MavenMetadata {
   
    Snapshot snapshot = null;
    Versioning version = null;
    Metadata md = null;
    String ext = null;
    long modTime = 0;
    
    public MavenMetadata(MavenArtifact artifact, MavenBuild build) {
        modTime = build.getTimeInMillis()+build.getDuration();

        md = new Metadata();
        snapshot = new Snapshot();
        version = new Versioning();
        md.setVersioning(version);
        version.setSnapshot(snapshot);

        md.setVersion(artifact.version);
        md.setArtifactId(artifact.artifactId);
        md.setGroupId(artifact.groupId);
        md.setModelVersion("1.1.0");
        md.setVersion(artifact.version);        
        
        snapshot.setBuildNumber(1000 + build.getNumber());
        snapshot.setLocalCopy(false);
        snapshot.setTimestamp(SnapshotTransformation.getUtcDateFormatter().format(new Date(modTime)));
        version.setLastUpdatedTimestamp(new Date(modTime));        
        SnapshotVersion snapVer = new SnapshotVersion();
        int dotindex = artifact.canonicalName.lastIndexOf('.');
        if (dotindex > 0) {
            ext = artifact.canonicalName.substring(dotindex+1);
        } else {
            ext = artifact.type;
        }
        snapVer.setClassifier(artifact.classifier);
        snapVer.setExtension(ext);
        snapVer.setUpdated(snapshot.getTimestamp());
        snapVer.setVersion(getUniqueVersion());
        
        version.addSnapshotVersion(snapVer);
        
    }
    protected String constructVersion(Snapshot snapshot, String baseVersion )
    {
        String version = null;
        if ( snapshot != null )
        {
            if ( snapshot.getTimestamp() != null && snapshot.getBuildNumber() > 0 )
            {
                String newVersion = snapshot.getTimestamp() + "-" + snapshot.getBuildNumber();
                version = StringUtils.replace( baseVersion, "SNAPSHOT", newVersion );
            }
            else
            {
                version = baseVersion;
            }
        }
        return version;
    }
    public String getUniqueVersion() {
        return constructVersion(snapshot, md.getVersion());
    }
    
    public String getUniqueName() {
        return md.getArtifactId()+"-"+getUniqueVersion()+"."+ext;
    }
    
    public Metadata getMetadata() {
        return md;
    }
}
