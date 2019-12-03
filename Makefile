JC	     = javac
CLASSPATH = -cp src/src/
RUNFLAGS = -Djava.security.policy=policy 
OBJECTS = src/src/*.java
TARGET  = classes
CLASS   = $(shell find . -type f -name "*.class" | tr '\n' ' ' | sed 's/\$$/\\$$/g')
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JCFLAGS) $*.java 

all: $(TARGET)

$(TARGET): $(OBJECTS:.java=.class)


run-server:
		java $(RUNFLAGS) $(CLASSPATH) Server 1997

run-client:
		java $(RUNFLAGS) $(CLASSPATH) DetectionNode 1

clean:
		$(RM) $(CLASS)
