#!/usr/bin/env ruby

base_dir = File.join(File.dirname(__FILE__),'../../..')
src_dir = File.join(base_dir, "/src/main/asciidoc")
require 'optparse'
require 'asciidoctor'

options = {}
file = "#{src_dir}/README.adoc"

OptionParser.new do |o|
  o.on('-o OUTPUT_FILE', 'Output file (default is stdout)') { |file| options[:to_file] = file unless file=='-' }
  o.on('-h', '--help') { puts o; exit }
  o.parse!
end

file = ARGV[0] if ARGV.length>0

# Copied from https://github.com/asciidoctor/asciidoctor-extensions-lab/blob/master/scripts/asciidoc-coalescer.rb
attributes = {}
attributes['project-root'] = File.expand_path("#{base_dir}/../")

doc = Asciidoctor.load_file file, safe: :unsafe, header_only: true, attributes: attributes
header_attr_names = (doc.instance_variable_get :@attributes_modified).to_a
header_attr_names.each {|k| doc.attributes[%(#{k}!)] = '' unless doc.attr? k }
attrs = doc.attributes
attrs['allow-uri-read'] = true
puts attrs

out = <<-WARNING
////
DO NOT EDIT THIS FILE. IT WAS GENERATED.
Manual changes to this file will be lost when it is generated again.
Edit the files in the src/main/asciidoc/ directory instead.
////

WARNING
doc = Asciidoctor.load_file file, safe: :unsafe, parse: false, attributes: attrs
out << doc.reader.read

unless options[:to_file]
  puts out
else
  File.open(options[:to_file],'w+') do |file|
    file.write(out)
  end
end