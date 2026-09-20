package com.demo_caffeine.demo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.cache.annotation.Cacheable;


// expireAfterWrite() or expireAfterAccess() -- cache time 

@Service
public class TextService {
	@Cacheable("texts")
    public String getText() throws IOException 
    {
		System.out.println("[*] Lecture du fichier") ; 
    	try (InputStream inputStream =
                getClass().getClassLoader().getResourceAsStream("texte.txt")) {

       if (inputStream == null) {
           throw new IOException("Le fichier text.txt est introuvable");
       }

       return new String(
               inputStream.readAllBytes(),
               StandardCharsets.UTF_8);
    	}
    }
    


}
