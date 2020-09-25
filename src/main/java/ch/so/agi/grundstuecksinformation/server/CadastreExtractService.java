package ch.so.agi.grundstuecksinformation.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

@Service
public class CadastreExtractService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Jaxb2Marshaller marshaller;

}
