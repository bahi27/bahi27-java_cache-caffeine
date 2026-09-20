package com.demo_caffeine.demo;

import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TextControler {
	public TextService ts ; 
	
	public TextControler(TextService ts) {
		this.ts = ts ; 
	}
	@GetMapping("/text")
	public TextResult getText() throws IOException {
		long debut = System.nanoTime() ;
		String text= ts.getText() ;
		long fin = System.nanoTime() ;
		long temps = (fin - debut ); 
		System.out.println("[+] Elapsed Time:  "+ temps+ "  ns" ) ;
	    TextResult tr = new TextResult(text,temps) ;
		return tr ; 
	}
	

}
