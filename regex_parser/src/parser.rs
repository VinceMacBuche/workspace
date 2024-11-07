
use nom::{
    branch::alt,
    bytes::complete::tag,
    character::complete::{satisfy},
    combinator::{map,value},
    multi::{many1},
    sequence::{terminated},
};

#[derive(Debug, PartialEq, Clone)]
pub struct Parsed(char);

#[derive(Debug, PartialEq, Clone)]
pub enum RegexItem {
    Char(char),
    Wildcard
}

#[derive(Debug, PartialEq, Clone)]
pub enum Regex {
    Value(RegexItem),
    ZeroOrMore(RegexItem)
}

// our matching function, we check token by token, probably quite naïve
pub fn match_string(s: &Vec<Parsed>, p : &Vec<Regex>)  -> bool {
    match (s.as_slice(),p.as_slice()) {
        // No more tokens
        ([],[]) => true,
        // match any character with a wildcard match next toke
        ( [ _ , t @ ..], [ Regex::Value(RegexItem::Wildcard), tr @ ..]) => match_string(&t.to_vec(),&tr.to_vec()),
        // match character with character from regexp, if ok go next
        ( [ Parsed(c) , t @ ..], [ Regex::Value(RegexItem::Char(cr)), tr @ ..]) => c == cr && match_string(&t.to_vec(),&tr.to_vec()),
        // Zero or more wild card, it will go to the end of the input or if it fails will backtrack and try to check current token with next parser token
        ( [ _ , t @ ..], [ Regex::ZeroOrMore(RegexItem::Wildcard), tr @ ..]) =>  match_string(&t.to_vec(),&p) || match_string(&s,&tr.to_vec()),
        // Zero or more char, it will go to the end of the input while character match or if it fails will backtrack and try to check current token with next parser token, if not match, go to next parser token
        ( [ Parsed(c) , t @ ..], [ Regex::ZeroOrMore(RegexItem::Char(cr)), tr @ ..]) => if c == cr { match_string(&t.to_vec(),&p) || match_string(&s,&tr.to_vec()) } else { match_string(&s,&tr.to_vec()) } ,
        // No more input, if in Zero or more, continue to match with next token
        ( [ ], [ Regex::ZeroOrMore(_), tr @ ..]) => match_string(&s,&tr.to_vec()),
        // No more input, but expect a character, it's an error
        ( [ ], [ Regex::Value(_), ..]) => false,
        // Still some input, no more regex, fails
        ( [_ , ..]  , [] ) => false ,
    }
}



// Parse a string into a parsed string token list
pub fn parse_string(i: &str) -> nom::IResult<&str, Vec<Parsed>> {
    // match only lowercase alpha
    let (s,r) = many1(map(satisfy(|c| c.is_alphabetic() & c.is_lowercase()),Parsed))(i)?;
    Ok((s,r))
  }

// Parse a string in a valid regex
pub fn parse_regex(i: &str) -> nom::IResult<&str, Vec<Regex>> {
  let (s,r) = many1(regex)(i)?;
  Ok((s,r))
}

// Treat regex item, wildcard and character
fn wildcard(i: &str) -> nom::IResult<&str, RegexItem> {
  value(RegexItem::Wildcard,tag("."))(i)
}

fn char_value(i: &str) -> nom::IResult<&str, RegexItem> {
  // match only lowercase alpha
  map(satisfy(|c| c.is_alphabetic() & c.is_lowercase()),RegexItem::Char)(i)
}

fn regex_item(i: &str) -> nom::IResult<&str, RegexItem> {
    alt((wildcard,char_value))(i)
}

// Treat regex element, either a single elem (regex value) or a zero_or_more (ca)
fn zero_or_more(i:  &str) -> nom::IResult<&str, Regex> {
  map(terminated(regex_item,tag("*")), Regex::ZeroOrMore)(i)
}

fn regex_value(i:  &str) -> nom::IResult<&str, Regex> {
    map(regex_item,Regex::Value)(i)
  }

fn regex(i:  &str) -> nom::IResult<&str, Regex> {
    alt((zero_or_more,regex_value))(i)
  }
    
  