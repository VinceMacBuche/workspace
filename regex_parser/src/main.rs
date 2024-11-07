
mod parser;

use std::io;

fn main() {
  let mut parser_string = String::new();
  let mut matched_string = String::new();
  println!("Enter parser string: ");
  io::stdin().read_line(&mut parser_string).expect("Failed to read line");
  parser_string = (&parser_string.trim()).to_string();

  println!("Enter matching string: ");
  io::stdin().read_line(&mut matched_string).expect("Failed to read line");
  matched_string = (&matched_string.trim()).to_string();
    
  if parser_string.len() > 20 {
    println!("parser string '{}' too long", parser_string);
  } else {
    if matched_string.len() > 20 {
      println!("matched string '{}' too long", matched_string);
    } else {
      match parser::parse_regex(&parser_string){
        Err(e) => println!("{}",e),
        Ok((rest_parser,p)) => { 
          if rest_parser.is_empty() {
            match parser::parse_string(&matched_string){
              Err(e) => println!("{}",e),
              Ok((rest_match,s)) => {
                if rest_match.is_empty() {
                  println!("{:?}",parser::match_string(&s,&p))
                } else {
                  println!("invalid string {}", matched_string);
                }
              }
            }
          } else {
            println!("invalid parser string {}", parser_string);
          }
        },
      };
    }
  }
}



