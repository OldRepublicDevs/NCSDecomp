import com.kotor.resource.formats.ncs.*;
import java.io.*;
import java.lang.reflect.*;
FileDecompiler fd = new FileDecompiler();
Field f = FileDecompiler.class.getDeclaredField("actions");
f.setAccessible(true);
System.out.println(f.get(fd));
/exit
