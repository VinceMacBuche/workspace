
use nom::{
    branch::alt,
    bytes::complete::tag,
    character::complete::{satisfy},
    combinator::{map,value},
    multi::{ many1},
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
    Star(RegexItem)
}


pub fn match_string(s: &Vec<Parsed>, p : &Vec<Regex>)  -> bool {
    match (s.as_slice(),p.as_slice()) {
        ([],[]) => true,
        ( [ _ , t @ ..], [ Regex::Value(RegexItem::Wildcard), tr @ ..]) => match_string(&t.to_vec(),&tr.to_vec()),
        ( [ Parsed(c) , t @ ..], [ Regex::Value(RegexItem::Char(cr)), tr @ ..]) => c == cr && match_string(&t.to_vec(),&tr.to_vec()),
        ( [ _ , t @ ..], [ Regex::Star(RegexItem::Wildcard), ..]) =>  match_string(&t.to_vec(),&p),
        ( [ Parsed(c) , t @ ..], [ Regex::Star(RegexItem::Char(cr)), tr @ ..]) => if c == cr { match_string(&t.to_vec(),&p) } else { match_string(&s,&tr.to_vec()) } ,
        ( [ ], [ Regex::Star(_), tr @ ..]) => match_string(&s,&tr.to_vec()),
        ( [ ], [ Regex::Value(_), ..]) => false,
        _ => false ,
    }
}


pub fn parse_string(i: &str) -> nom::IResult<&str, Vec<Parsed>> {
    let (s,r) = many1(map(satisfy(|c| c.is_alphabetic() & c.is_lowercase()),Parsed))(i)?;
    Ok((s,r))
  }


pub fn parse_regex(i: &str) -> nom::IResult<&str, Vec<Regex>> {
  let (s,r) = many1(regex)(i)?;
  Ok((s,r))
}
fn wildcard(i: &str) -> nom::IResult<&str, RegexItem> {
  value(RegexItem::Wildcard,tag("."))(i)
}

fn char_value(i: &str) -> nom::IResult<&str, RegexItem> {
  map(satisfy(|c| c.is_alphabetic() & c.is_lowercase()),RegexItem::Char)(i)
}

fn regex_item(i: &str) -> nom::IResult<&str, RegexItem> {
    alt((wildcard,char_value))(i)
}


fn star(i:  &str) -> nom::IResult<&str, Regex> {
  map(terminated(regex_item,tag("*")), Regex::Star)(i)
}

fn regex_value(i:  &str) -> nom::IResult<&str, Regex> {
    map(regex_item,Regex::Value)(i)
  }

fn regex(i:  &str) -> nom::IResult<&str, Regex> {
    alt((star,regex_value))(i)
  }
    
  