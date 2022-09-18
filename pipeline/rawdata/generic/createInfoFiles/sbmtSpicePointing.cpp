#include "sbmt_spice.hpp"

#include "SpiceUsr.h"
#include "cfitsio/fitsio.h"

#include <iostream>
#include <iterator>
#include <map>
#include <sstream>
#include <stdexcept>
#include <string>
#include <utility>

namespace {

const char * const g_programName = "sbmtValidateSpice";
int g_status = 0;

enum CLParId {
  BODY,
  BODY_FRAME,
  IMAGE,
  INSTR_FRAME,
  MK,
  OUT_FILE,
  SC_ID,
  TIME,
  NUMBER_PARS
};

const char * const CLFlags[] = {
  "b",
  "bf",
  "img",
  "ins",
  "mk",
  "of",
  "sc",
  "t"
};

SpiceChar ERROR_REPORT[] = "SHORT";
SpiceChar RETURN[] = "RETURN";

}

namespace sbmt_spice {

class InvalidCommandLine : public std::runtime_error {
public:
  InvalidCommandLine(const std::string & what): std::runtime_error(what) {}

  InvalidCommandLine(const InvalidCommandLine & source): std::runtime_error(source) {}
};

struct CommandLinePar {
  CommandLinePar(const std::string & key): m_key(key), m_value(), m_isKey(false) {}

  CommandLinePar(const std::string & key, const std::string & value, bool isValueKeywordName):
    m_key(key), m_value(value), m_isKey(isValueKeywordName) {}

  CommandLinePar(const CommandLinePar & source): m_key(source.m_key), m_value(source.m_value), m_isKey(source.m_isKey) {}

  CommandLinePar & operator=(const CommandLinePar & source) {
    m_key = source.m_key;
    m_value = source.m_value;
    m_isKey = source.m_isKey;

    return *this;
  }

  std::string m_key;
  std::string m_value;
  bool m_isKey;
};

class CommandLineParameters {
public:
  typedef std::map<CLParId, const CommandLinePar *> MapType;

  CommandLineParameters(const MapType * pars): m_pars(pars) {}

  virtual ~CommandLineParameters() {
    if (m_pars != nullptr) {
      for (MapType::const_reverse_iterator itor = m_pars->rbegin(); itor != m_pars->rend(); ++itor) {
        delete itor->second;
      }
      delete m_pars;
    }
  }

  const CommandLinePar& operator[](CLParId parId) const {
    return *m_pars->at(parId);
  }

private:
  const MapType * m_pars;
};

class SpicePointingApp {
public:
  typedef std::map<CLParId, const CommandLinePar *> MapType;

  SpicePointingApp(const std::string & appName, const MapType * pars): m_appName(appName), m_pars(pars) {}

  virtual ~SpicePointingApp() {
    if (m_pars != nullptr) {
      for (MapType::const_reverse_iterator itor = m_pars->rbegin(); itor != m_pars->rend(); ++itor) {
        delete itor->second;
      }
      delete m_pars;
    }
  }

  const std::string & getAppName() const {
    return m_appName;
  }

  virtual void execute() {
  }

private:
  SpicePointingApp(const SpicePointingApp &): m_pars() {}

  SpicePointingApp & operator=(const SpicePointingApp &) { return *this; }

  const std::string m_appName;
  const MapType * m_pars;
};

SpicePointingApp * configureApp(int argc, char ** argv);
CommandLineParameters * interpretCommandLine(int argc, char ** argv);
void computePointing(const CommandLineParameters & pars);
void usage(std::ostream & ios);
SpiceDouble parseET(const std::string & time);
bool isSpiceError();

// namespace sbmt_spice
////////////////////////////////////////////////////////////////////////////////
}

int main(int argc, char ** argv) {
  using namespace sbmt_spice;
  using namespace std;

  // Return rather than abort after a SPICE error so the code may take additional actions.
  erract_c("SET", 1, RETURN);
  // Make mssages shorter -- but SPICE seems to ignore this.
  errprt_c("SET", 1, ERROR_REPORT);

  SpicePointingApp * app = nullptr;
  CommandLineParameters * pars = nullptr;
  try {
    app = configureApp(argc, argv);
    app->execute();

    pars = interpretCommandLine(argc, argv);

    computePointing(*pars);
  } catch (const InvalidCommandLine & e) {
    usage(cerr);
    cerr << g_programName << " caught exception: " << e.what() << endl;
    g_status = 1;
  } catch (const exception & e) {
    cerr << g_programName << " caught exception: " << e.what() << endl;
    g_status = 1;
  } catch (...) {
    cerr << g_programName << " caught object of unknown type." << endl;
    g_status = 1;
  }

  try {
    delete pars;
    delete app;
  } catch (const exception & e) {
    cerr << g_programName << " during clean-up caught exception: " << e.what() << endl;
    g_status = 1;
  } catch (...) {
    cerr << g_programName << " caught object of unknown type during clean-up." << endl;
    g_status = 1;
  }

  return g_status;
}

std::ostream & operator<<(std::ostream & os, const sbmt_spice::CommandLinePar & par) {
  os << "Par \"" << par.m_key << "\" = \"" << par.m_value << "\"";
  return os;
}

namespace {

std::string trim(const std::string & s) {
  std::string::const_iterator begin(s.begin());
  std::string::const_iterator end(s.end());

  while (begin != end && std::isspace(*begin)) {
    ++begin;
  }

  while (begin != end && std::isspace(*end)) {
    --end;
  }

  return std::string(begin, end);
}

void reportMissingPar(const sbmt_spice::CommandLinePar & par, std::stringstream & ss, std::string & delim) {
  if (par.m_value.empty()) {
    ss << delim << "-" << par.m_key;
    if (par.m_key != CLFlags[IMAGE]) {
      ss << " (or -k" << par.m_key << ")";
    }
    delim = ", ";
  }
}

bool extractPar(sbmt_spice::CommandLinePar & par, const std::string & arg) {
  sbmt_spice::CommandLinePar newPar(par);

  bool isKey = false;
  bool foundMatch = false;

  std::string flag("-" + par.m_key + "=");
  std::string::size_type flagSize(flag.size());

  if (arg.compare(0, flagSize, flag) == 0) {
    newPar.m_value = arg.substr(flagSize);
    foundMatch = true;
  } else if (par.m_key != CLFlags[IMAGE]) {

    std::string flag("-k" + par.m_key + "=");
    flagSize = flag.size();

    if (arg.compare(0, flagSize, flag) == 0) {
      newPar.m_value = arg.substr(flagSize);
      newPar.m_isKey = true;

      isKey = true;
      foundMatch = true;
    }
  }

  if (foundMatch) {
    if (!par.m_value.empty()) {
      std::stringstream ss;
      ss << "Attempted to parse argument \"" + arg + "\" into already-defined parameter " << par;
      throw std::invalid_argument(ss.str());
    }

    par = newPar;
  }

  return isKey;
}

void extractPar(sbmt_spice::CommandLinePar & par, int argc, char ** argv) {
  for (int i = 1; i < argc; ++i) {
    extractPar(par, argv[i]);
  }
}

// namespace
////////////////////////////////////////////////////////////////////////////////
}

namespace sbmt_spice {

SpicePointingApp * configureApp(int argc, char ** argv) {
  using namespace std;

  SpicePointingApp::MapType * pars = new SpicePointingApp::MapType();

  for (int i = 0; i < NUMBER_PARS; ++i) {
    const CLParId id = static_cast<CLParId>(i);

    const string & key(CLFlags[i]);
    CommandLinePar * par = new CommandLinePar(key);
    
    extractPar(*par, argc, argv);

    pars->insert(make_pair(id, par));
  }

  return new SpicePointingApp(argv[0], pars);
}

CommandLineParameters * interpretCommandLine(int argc, char ** argv) {
  using namespace std;

  CommandLineParameters::MapType * pars = new CommandLineParameters::MapType();

  bool isKey = false;
  for (int i = 0; i < NUMBER_PARS; ++i) {
    const CLParId id = static_cast<CLParId>(i);

    const string & key(CLFlags[i]);
    CommandLinePar * par = new CommandLinePar(key);
    
    extractPar(*par, argc, argv);

    isKey |= par->m_isKey;

    pars->insert(make_pair(id, par));
  }

  CommandLinePar body(CLFlags[BODY]);
  CommandLinePar bodyFrame(CLFlags[BODY_FRAME]);
  CommandLinePar imageFile(CLFlags[IMAGE]);
  CommandLinePar mkFile(CLFlags[MK]);
  CommandLinePar outputFile(CLFlags[OUT_FILE]);
  CommandLinePar scId(CLFlags[SC_ID]);
  CommandLinePar time(CLFlags[TIME]);

  for (int i = 1; i < argc; ++i) {
    string arg = trim(argv[i]);

    isKey |= extractPar(body, arg);
    isKey |= extractPar(bodyFrame, arg);
    isKey |= extractPar(imageFile, arg);
    isKey |= extractPar(mkFile, arg);
    isKey |= extractPar(outputFile, arg);
    isKey |= extractPar(scId, arg);
    isKey |= extractPar(time, arg);
  }

  stringstream ss;
  string delim;

  if (isKey && imageFile.m_value.empty()) {
    reportMissingPar(imageFile, ss, delim);
    ss << " (image file required if using any -k options)";
  }

  reportMissingPar(*pars->at(BODY), ss, delim);
  reportMissingPar(*pars->at(BODY_FRAME), ss, delim);
  reportMissingPar(*pars->at(INSTR_FRAME), ss, delim);
  reportMissingPar(*pars->at(MK), ss, delim);
  reportMissingPar(*pars->at(SC_ID), ss, delim);
  reportMissingPar(*pars->at(TIME), ss, delim);

  string missing = ss.str();
  if (!missing.empty()) {
    throw InvalidCommandLine("Missing required argument(s): " + missing);
  }

  return new CommandLineParameters(pars);
}

void computePointing(const CommandLineParameters & pars) {
  using namespace std;

  const char * body(pars[BODY].m_value.c_str());
  const char * bodyFrame(pars[BODY_FRAME].m_value.c_str());
  const char * instr(pars[INSTR_FRAME].m_value.c_str());
  const char * mkFile(pars[MK].m_value.c_str());
  const char * scId(pars[SC_ID].m_value.c_str());
  const char * time(pars[TIME].m_value.c_str());

  furnsh_c(mkFile);
  if (isSpiceError()) {
    stringstream ss;
    ss << "Unable to initialize SPICE environment using metakernel file \"" << mkFile << "\"";
    throw runtime_error(ss.str());
  }

  double et = parseET(time);
  if (isSpiceError()) {
    stringstream ss;
    ss << "Unable to parse time \"" << time << "\"";
    throw runtime_error(ss.str());
  }

  double bodyToSc[3];
  double unused[3];
  double boredir[3];
  double updir[3];
  double frustum[12];
  double sunPosition[3];

  getSpacecraftState(et, scId, body, bodyFrame, bodyToSc, unused);
  if (isSpiceError()) {
    stringstream ss;
    ss << "Unable to get position of spacecraft " << scId << " in the " << string(bodyFrame) + " frame";
    throw runtime_error(ss.str());
  }

  getTargetState(et, scId, body, bodyFrame, "SUN", sunPosition, unused);
  if (isSpiceError()) {
    stringstream ss;
    ss << "Unable to get the sun position in the " << string(bodyFrame)+ " frame";
    throw runtime_error(ss.str());
  }

   getFov(et, scId, body, bodyFrame, instr, boredir, updir, frustum);
  if (isSpiceError()) {
    stringstream ss;
    ss << "Unable to get the " << instr << " FOV in the " << string(bodyFrame) + " frame";
    throw runtime_error(ss.str());
  }

}

void usage(std::ostream & ios) {
  std::stringstream ss;

  ss << "Usage: " << g_programName << " ";

  ss << std::endl;
  ios << ss.str();
}

SpiceDouble parseET(const std::string & time) {
  SpiceDouble et;

  str2et_c(time.c_str(), &et);
  if (isSpiceError()) {
    throw std::runtime_error("Unable to parse an ET from the string " + time);
  }

  return et;
}

bool isSpiceError() {
  bool error = failed_c() != 0;
  reset_c();

  return error;
}

// namespace sbmt_spice
////////////////////////////////////////////////////////////////////////////////
}
